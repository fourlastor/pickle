package com.fourlastor.pickle

import cucumber.runtime.model.CucumberFeature

class ClassConverter(
        private val methodsConverter: MethodsConverter,
        private val statementHooksCreator: StatementHooksCreator
) {

    fun convert(feature: CucumberFeature): TestClass {
        val methods = methodsConverter.convert(feature.featureElements)
        val before = beforeHookMethod(statementHooksCreator.createBeforeHooks())
        val after = afterHookMethod(statementHooksCreator.createAfterHooks())

        val fields = methods.flatMap { it.statements }
                .plus(before.statements)
                .plus(after.statements)
                .map { it.field }
                .toSet()

        return TestClass(
                "${feature.gherkinFeature.name.toCamelCase()}Test",
                listOf(before, after),
                methods,
                fields
        )
    }
}
