package com.fourlastor.pickle

import gherkin.ast.Background
import gherkin.ast.Scenario
import gherkin.ast.Step


class MethodConverter(private val statementConverter: StatementConverter) {

    fun convert(name: String, scenario: Scenario, background: Background?): ImplementedTestMethod {
        val statements = merge(background, scenario)
            .map(statementConverter::convert)
        return ImplementedTestMethod(name, statements)
    }

    fun ignoredTest(name: String, scenarioName: String) = IgnoredTestMethod(name, scenarioName)

}

private fun merge(background: Background?, scenario: Scenario): List<Step> =
    background?.steps.orEmpty() + scenario.steps
