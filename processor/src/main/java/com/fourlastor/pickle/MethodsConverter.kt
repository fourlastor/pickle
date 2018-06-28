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
        return statements.flatMap {
            when (it) {
                is CucumberScenario -> {
                    val methodName = it.gherkinModel.name.toCamelCase().decapitalize()
                    val method = it.convertToMethod(methodName)
                    Collections.singletonList(method)
                }
                is CucumberScenarioOutline -> {
                    it.cucumberExamplesList.flatMap {
                        it.createExampleScenarios().mapIndexed { index, scenario ->
                            val methodName = "${scenario.gherkinModel.name.toCamelCase().decapitalize()}$index"
                            scenario.convertToMethod(methodName)
                        }
                    }
                }
                else -> throw UnsupportedStatementException(it::class.java.name)
            }
        }.filterNotNull()
    }

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
