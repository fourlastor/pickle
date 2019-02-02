package com.fourlastor.pickle

import cucumber.runtime.model.CucumberScenario
import cucumber.runtime.model.CucumberScenarioOutline
import cucumber.runtime.model.CucumberTagStatement
import java.util.Collections

class MethodsConverter(
        private val methodConverter: MethodConverter,
        private val strictMode: Boolean
) {
    fun convert(statements: List<CucumberTagStatement>): List<TestMethod> {
        val duplicates = statements.findDuplicates()
        if (duplicates.isNotEmpty()) {
            throw DuplicateScenarioException(duplicates)
        }

        return statements.flatMap { statement ->
            when (statement) {
                is CucumberScenario -> {
                    val methodName = statement.gherkinModel.name.toCamelCase().decapitalize()
                    val method = statement.convertToMethod(methodName)
                    Collections.singletonList(method)
                }
                is CucumberScenarioOutline -> {
                    statement.cucumberExamplesList.flatMap {
                        it.createExampleScenarios().mapIndexed { index, scenario ->
                            val methodName = "${scenario.gherkinModel.name.toCamelCase().decapitalize()}$index"
                            scenario.convertToMethod(methodName)
                        }
                    }
                }
                else -> throw UnsupportedStatementException(statement::class.java.name)
            }
        }.filterNotNull()
    }

    private fun List<CucumberTagStatement>.findDuplicates() =
            groupingBy { it.visualName }.eachCount().filterValues { it > 1 }.keys

    private fun CucumberScenario.convertToMethod(name: String): TestMethod? {
        return try {
            methodConverter.convert(name, this)
        } catch (e: MissingStepDefinitionException) {
            if (strictMode) {
                propagate(e)
            }
            skipMethod()
        }
    }

    private fun propagate(e: MissingStepDefinitionException): Nothing = throw e

    private fun skipMethod() = null
}

class UnsupportedStatementException(type: String) : IllegalArgumentException("Statements of type \"$type\" aren't supported")

class DuplicateScenarioException(duplicateScenarios: Collection<String>) : RuntimeException("""
    Scenarios need to have unique names.
    Duplicate scenarios:
    ${duplicateScenarios.joinToString(separator = "\n") { "> $it" }}
    """.trimIndent()
)
