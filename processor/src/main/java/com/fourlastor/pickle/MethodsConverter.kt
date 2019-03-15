package com.fourlastor.pickle

import cucumber.runtime.model.CucumberScenario
import cucumber.runtime.model.CucumberScenarioOutline
import cucumber.runtime.model.CucumberTagStatement
import java.util.*
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

class MethodsConverter(
        private val methodConverter: MethodConverter,
        private val strictMode: Boolean,
        private val messager: Messager
) {
    fun convert(statements: List<CucumberTagStatement>): List<TestMethod> {
        val duplicates = statements.findDuplicates()
        if (duplicates.isNotEmpty()) {
            throw DuplicateScenarioException(duplicates)
        }

        return statements.flatMap { statement ->
            when (statement) {
                is CucumberScenario -> {
                    val scenarioName = statement.gherkinModel.name
                    val methodName = scenarioName.toCamelCase().decapitalize()
                    val method = statement.convertToMethod(methodName, scenarioName)
                    Collections.singletonList(method)
                }
                is CucumberScenarioOutline -> {
                    statement.cucumberExamplesList.flatMap {
                        it.createExampleScenarios().mapIndexed { index, scenario ->
                            val scenarioName = scenario.gherkinModel.name
                            val methodName = "${scenarioName.toCamelCase().decapitalize()}$index"
                            scenario.convertToMethod(methodName, scenarioName)
                        }
                    }
                }
                else -> throw UnsupportedStatementException(statement::class.java.name)
            }
        }.filterNotNull()
    }

    private fun List<CucumberTagStatement>.findDuplicates() =
            groupingBy { it.visualName }.eachCount().filterValues { it > 1 }.keys

    private fun CucumberScenario.convertToMethod(name: String, scenarioName: String): TestMethod {
        return try {
            methodConverter.convert(name, this)
        } catch (e: MissingStepDefinitionException) {
            if (strictMode) {
                propagate(e)
            }
            methodConverter.ignoredTest(name, scenarioName).also {
                messager.warning("""
                    ${e.message}
                    "${gherkinModel.keyword}: ${gherkinModel.name}" will be skipped.
                """.trimIndent())
            }
        }
    }

    private fun propagate(e: MissingStepDefinitionException): Nothing = throw e

    private fun Messager.warning(message: String) {
        printMessage(Diagnostic.Kind.WARNING, message)
    }
}

class UnsupportedStatementException(type: String) : IllegalArgumentException("Statements of type \"$type\" aren't supported")

class DuplicateScenarioException(duplicateScenarios: Collection<String>) : RuntimeException("""
    Scenarios need to have unique names.
    Duplicate scenarios:
    ${duplicateScenarios.joinToString(separator = "\n") { "> $it" }}
    """.trimIndent()
)
