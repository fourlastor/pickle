package com.fourlastor.pickle

import cucumber.runtime.model.CucumberScenario

class MethodConverter(private val statementConverter: StatementConverter) {

    fun convert(name: String, scenario: CucumberScenario) =
            ImplementedTestMethod(name, createStatementsFor(scenario))

    fun ignoredTest(name: String, scenarioName: String) = IgnoredTestMethod(name, scenarioName)

    private fun createStatementsFor(scenario: CucumberScenario): List<TestMethodStatement> {
        return scenario.stepsIncludingBackground()
                .map { statementConverter.convert(it) }
    }
}

private fun CucumberScenario.stepsIncludingBackground() = cucumberBackground?.steps.orEmpty() + steps
