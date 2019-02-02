package com.fourlastor.pickle

import cucumber.runtime.model.CucumberScenario

class MethodConverter(
        private val statementConverter: StatementConverter,
        private val statementHooksCreator: StatementHooksCreator
) {

    fun convert(name: String, scenario: CucumberScenario): TestMethod {
        val statements = createStatementsFor(scenario)
        return TestMethod(name, statements)
    }

    private fun createStatementsFor(scenario: CucumberScenario): Set<TestMethodStatement> {
        val methodStatements = scenario.stepsIncludingBackground()
                .map { statementConverter.convert(it) }

        val beforeStatements = statementHooksCreator.createBeforeHooks(methodStatements)

        val afterStatements = statementHooksCreator.createAfterHooks(methodStatements)

        return beforeStatements + methodStatements + afterStatements
    }
}

private fun CucumberScenario.stepsIncludingBackground() = (cucumberBackground?.steps.orEmpty()) + steps
