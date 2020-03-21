package com.fourlastor.pickle

import gherkin.ast.Background
import gherkin.ast.Scenario
import gherkin.ast.ScenarioDefinition
import gherkin.ast.ScenarioOutline
import java.util.*
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

class MethodsConverter(
        private val methodConverter: MethodConverter,
        private val strictMode: Boolean,
        private val messager: Messager
) {

    fun convert(statements: List<ScenarioDefinition>): List<TestMethod> {
        val background = statements.filterIsInstance<Background>().firstOrNull()
        return convert(background, statements.filterNot { it == background })
    }

    private fun convert(background: Background?, scenarios: List<ScenarioDefinition>): List<TestMethod> {
        val duplicates = scenarios.findDuplicates()
        if (duplicates.isNotEmpty()) {
            throw DuplicateScenarioException(duplicates)
        }

        return scenarios.flatMap { scenario ->
            when (scenario) {
                is Scenario -> {
                    val method = scenario.convertToMethod(background)
                    Collections.singletonList(method)
                }
                is ScenarioOutline -> {
                    scenario.examples.flatMap {
                        it.toScenarios(scenario).mapIndexed { index, scenario ->
                            scenario.convertToMethod(background, index.toString())
                        }
                    }
                }
                else -> throw UnsupportedStatementException(scenario::class.java.name)
            }
        }.filterNotNull()
    }

    private fun List<ScenarioDefinition>.findDuplicates() =
            groupingBy { it.name }.eachCount().filterValues { it > 1 }.keys

    private fun Scenario.convertToMethod(background: Background?, methodSuffix: String = ""): TestMethod {
        val methodName = name.toCamelCase().decapitalize() + methodSuffix
        return try {
            methodConverter.convert(methodName, this, background)
        } catch (e: MissingStepDefinitionException) {
            if (strictMode) {
                propagate(e)
            }
            methodConverter.ignoredTest(methodName, name).also {
                messager.warning(
                        """
                    ${e.message}
                    "$keyword: $name" will be skipped.
                    
                """.trimIndent()
                )
            }
        }
    }

    private fun propagate(e: MissingStepDefinitionException): Nothing = throw e

    private fun Messager.warning(message: String) {
        printMessage(Diagnostic.Kind.WARNING, message)
    }
}

class UnsupportedStatementException(type: String) : PickleException("Statements of type \"$type\" aren't supported")

class DuplicateScenarioException(duplicateScenarios: Collection<String>) : PickleException(
        """
    Scenarios need to have unique names.
    Duplicate scenarios:
    ${duplicateScenarios.joinToString(separator = "\n") { "> $it" }}
    """.trimIndent()
)
