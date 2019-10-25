package com.fourlastor.pickle

import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import gherkin.formatter.model.Step
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

class StatementConverter(roundEnv: RoundEnvironment) {

    private val stepDefinitions = roundEnv.getStepDefinitions()

    fun convert(step: Step): TestMethodStatement {
        val matching = stepDefinitions.filter { it.regex.matches(step.name) }
        if (matching.isEmpty()) {
            throw MissingStepDefinitionException(step.name)
        }
        if (matching.size > 1) {
            throw AmbiguousStepDefinitionException(step.name, matching.map(StepDefinition::regex))
        }
        val stepDefinition = matching[0]
        val field = createFieldFor(stepDefinition)
        return createStatement(stepDefinition, step, field)
    }

    private fun createFieldFor(stepDefinition: StepDefinition): TestField {
        val type = stepDefinition.element.enclosingElement as TypeElement
        return TestField(type)
    }

    private fun createStatement(stepDefinition: StepDefinition, step: Step, stepsField: TestField): TestMethodStatement {
        val matches = stepDefinition.regex.matchEntire(step.name) ?: throw MissingStepDefinitionException(step.name)

        val parameterValues = matches.groupValues.drop(1).toTypedArray()
        val parameterTypes = stepDefinition.element.parameters.map { it.asType() }
        if (parameterTypes.size != parameterValues.size) {
            throw StepDefinitionArgumentsMismatchException(step.name, stepDefinition.element)
        }
        val parameterStringFormat = parameterTypes.joinToString(", ") { if (it.toString() == String::class.java.name) "\$S" else "\$L" }
        return testMethodStatement(
                testField = stepsField,
                statementFormat = "\$N.\$N($parameterStringFormat)",
                methodName = stepDefinition.element.simpleName,
                args = *parameterValues
        )
    }
}

private fun RoundEnvironment.getStepDefinitions(): List<StepDefinition> {
    val givens = getElementsAnnotatedWith(Given::class.java)
            .filter { it.kind == ElementKind.METHOD }
            .map { StepDefinition(it as ExecutableElement, Regex(it.getAnnotation(Given::class.java).value)) }

    val thens = getElementsAnnotatedWith(Then::class.java)
            .filter { it.kind == ElementKind.METHOD }
            .map { StepDefinition(it as ExecutableElement, Regex(it.getAnnotation(Then::class.java).value)) }

    val whens = getElementsAnnotatedWith(When::class.java)
            .filter { it.kind == ElementKind.METHOD }
            .map { StepDefinition(it as ExecutableElement, Regex(it.getAnnotation(When::class.java).value)) }

    val ands = getElementsAnnotatedWith(And::class.java)
            .filter { it.kind == ElementKind.METHOD }
            .map { StepDefinition(it as ExecutableElement, Regex(it.getAnnotation(And::class.java).value)) }

    return givens + thens + whens + ands
}

private data class StepDefinition(val element: ExecutableElement, val regex: Regex)

class StepDefinitionArgumentsMismatchException(stepName: String, element: ExecutableElement) : PickleException("""
    Step definition argument mismatch.
    > Step definition: "$stepName"
    > Step implementation: ${element.enclosingElement}.$element
""".trimIndent())

class AmbiguousStepDefinitionException(stepName: String, matching: List<Regex>) : RuntimeException("""
    Multiple step implementations matched.
    > Step definition: "$stepName"
    > Step implementation with regexes:
    ${matching.joinToString(separator = "\n    ")}
""".trimIndent())
