package com.fourlastor.pickle

import cucumber.runtime.model.CucumberFeature

class ClassConverter(
        private val methodsConverter: MethodsConverter,
        private val hooksCreator: HooksCreator
) {

    fun convert(feature: CucumberFeature): TestClass {
        val methods = methodsConverter.convert(feature.featureElements)
        val hooks = hooksCreator.create()

        val fields = methods.filterIsInstance(ImplementedTestMethod::class.java).flatMap { it.statements }
                .plus(hooks.flatMap { it.statements })
                .map { it.field }
                .toSet()

        return TestClass(
                "${feature.gherkinFeature.name.toCamelCase()}Test",
                hooks,
                methods,
                fields
        )
    }
}
