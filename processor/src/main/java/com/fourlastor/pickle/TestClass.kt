package com.fourlastor.pickle

import com.squareup.javapoet.ClassName
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement

data class TestClass(
        val name: String,
        val hookMethods: List<HookMethod>,
        val methods: List<TestMethod>,
        val fields: Set<TestField>
)

data class HookMethod(val annotation: ClassName, val name: String, val statements: List<TestMethodStatement>)
data class TestMethod(val name: String, val statements: List<TestMethodStatement>)
data class TestMethodStatement(val field: TestField, val statementFormat: String, val args: List<Any>)
data class TestField(val type: TypeElement) {
    val name: String
        get() = type.qualifiedName.toString().decapitalize().replace('.', '_')
}

fun testMethodStatement(testField: TestField, statementFormat: String, methodName: Name, vararg args: Any) =
        TestMethodStatement(testField, statementFormat, listOf(testField.name, methodName) + args)

fun beforeHookMethod(statements: List<TestMethodStatement>) =
        HookMethod(ClassName.bestGuess("org.junit.Before")!!, "setUp", statements)

fun afterHookMethod(statements: List<TestMethodStatement>) =
        HookMethod(ClassName.bestGuess("org.junit.After")!!, "tearDown", statements)
