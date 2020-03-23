package com.fourlastor.pickle

import gherkin.ast.Examples
import gherkin.ast.Scenario
import gherkin.ast.ScenarioOutline
import gherkin.ast.Step
import gherkin.ast.TableCell
import gherkin.ast.TableRow

internal fun Examples.toScenarios(outline: ScenarioOutline): List<Scenario> {
    return tableBody.map { example ->
        outline.createExampleScenario(tableHeader, example)
    }
}

private fun ScenarioOutline.createExampleScenario(header: TableRow, example: TableRow): Scenario {
    return Scenario(
        tags,
        example.location,
        keyword,
        name.replaceTokens(header.cells, example.cells),
        "",
        steps.map { it.createExampleStep(header, example) }
    )
}

private fun Step.createExampleStep(header: TableRow, example: TableRow): Step =
    Step(location, keyword, this.text.replaceTokens(header.cells, example.cells), null)

private fun String.replaceTokens(headerCells: List<TableCell>, exampleCells: List<TableCell>): String {
    var text = this
    headerCells.zip(exampleCells).forEach { (header, value) ->
        val token = "<${header.value}>"
        if (text.contains(token)) {
            text = text.replace(token, value.value)
        }
    }
    return text
}
