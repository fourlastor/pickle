package com.fourlastor.pickle

import javax.lang.model.element.TypeElement

data class TestClass(val name: String, val methods: List<TestMethod>, val fields: Set<TestField>)
data class TestMethod(val name: String, val statements: Set<TestMethodStatement>)
data class TestMethodStatement(val field: TestField, val statementFormat: String, val args: List<Any>)
data class TestField(val type: TypeElement) {
    val name: String
            get() = type.qualifiedName.toString().decapitalize().replace('.', '_')
}

fun testMethodStatement(field: TestField, statementFormat: String, vararg args: Any) =
        TestMethodStatement(field, statementFormat, args.asList())
