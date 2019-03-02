package com.fourlastor.pickle

import cucumber.runtime.model.CucumberScenario

class MethodConverter(private val statementConverter: StatementConverter) {

    fun convert(name: String, scenario: CucumberScenario): TestMethod {
        val statements = createStatementsFor(scenario)
        return TestMethod(name, statements)
    }

    private fun createStatementsFor(scenario: CucumberScenario): List<TestMethodStatement> {
        return scenario.stepsIncludingBackground()
                .map { statementConverter.convert(it) }
    }
}

private fun CucumberScenario.stepsIncludingBackground() = cucumberBackground?.steps.orEmpty() + steps
