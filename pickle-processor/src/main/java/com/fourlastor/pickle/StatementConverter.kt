package com.fourlastor.pickle

import gherkin.ast.Step
import io.cucumber.java.en.And
import io.cucumber.java.en.But
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

class StatementConverter(roundEnv: RoundEnvironment) {

    private val stepDefinitions = roundEnv.getStepDefinitions()

    fun convert(step: Step): TestMethodStatement {
        val matching = stepDefinitions.filter { it.regex.matches(step.text) }
        if (matching.isEmpty()) {
            throw MissingStepDefinitionException(step.text)
        }
        if (matching.size > 1) {
            throw AmbiguousStepDefinitionException(step.text, matching.map(StepDefinition::regex))
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
        val matches = stepDefinition.regex.matchEntire(step.text) ?: throw MissingStepDefinitionException(step.text)

        val parameterValues = matches.groupValues.drop(1).toTypedArray()
        val parameterTypes = stepDefinition.element.parameters.map { it.asType() }
        if (parameterTypes.size != parameterValues.size) {
            throw StepDefinitionArgumentsMismatchException(step.text, stepDefinition.element)
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

@Suppress("TYPEALIAS_EXPANSION_DEPRECATION")
private fun RoundEnvironment.getStepDefinitions(): List<StepDefinition> {
    return ArrayList<StepDefinition>().apply {
        this += getMethodsAnnotatedWith(Given::class.java)
                .map { StepDefinition(it, Regex(it.getAnnotation(Given::class.java).value)) }

        this += getMethodsAnnotatedWith(DeprecatedGiven::class.java)
                .map { StepDefinition(it, Regex(it.getAnnotation(DeprecatedGiven::class.java).value)) }

        this += getMethodsAnnotatedWith(Then::class.java)
                .map { StepDefinition(it, Regex(it.getAnnotation(Then::class.java).value)) }

        this += getMethodsAnnotatedWith(DeprecatedThen::class.java)
                .map { StepDefinition(it, Regex(it.getAnnotation(DeprecatedThen::class.java).value)) }

        this += getMethodsAnnotatedWith(When::class.java)
                .map { StepDefinition(it, Regex(it.getAnnotation(When::class.java).value)) }

        this += getMethodsAnnotatedWith(DeprecatedWhen::class.java)
                .map { StepDefinition(it, Regex(it.getAnnotation(DeprecatedWhen::class.java).value)) }

        this += getMethodsAnnotatedWith(And::class.java)
                .map { StepDefinition(it, Regex(it.getAnnotation(And::class.java).value)) }

        this += getMethodsAnnotatedWith(DeprecatedAnd::class.java)
                .map { StepDefinition(it, Regex(it.getAnnotation(DeprecatedAnd::class.java).value)) }

        this += getMethodsAnnotatedWith(But::class.java)
                .map { StepDefinition(it, Regex(it.getAnnotation(But::class.java).value)) }

        this += getMethodsAnnotatedWith(DeprecatedBut::class.java)
                .map { StepDefinition(it, Regex(it.getAnnotation(DeprecatedBut::class.java).value)) }
    }
}

private data class StepDefinition(val element: ExecutableElement, val regex: Regex)

class StepDefinitionArgumentsMismatchException(stepName: String, element: ExecutableElement) : PickleException("""
    Step definition argument mismatch.
    > Step definition: "$stepName"
    > Step implementation: ${element.enclosingElement}.$element
""".trimIndent())

class AmbiguousStepDefinitionException(stepName: String, matching: List<Regex>) : PickleException("""
    Multiple step implementations matched.
    > Step definition: "$stepName"
    > Step implementation with regexes:
    ${matching.joinToString(separator = "\n    ")}
""".trimIndent())
