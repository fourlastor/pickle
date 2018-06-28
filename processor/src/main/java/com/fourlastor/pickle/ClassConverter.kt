package com.fourlastor.pickle

import cucumber.runtime.model.CucumberFeature

class ClassConverter(private val methodsConverter: MethodsConverter) {

    fun convert(feature: CucumberFeature): TestClass {
        val methods = methodsConverter.convert(feature.featureElements)
        val fields = methods.flatMap { it.statements.map { it.field } }.toSet()

        return TestClass(
                "${feature.gherkinFeature.name.toCamelCase()}Test",
                methods,
                fields
        )
    }
}
