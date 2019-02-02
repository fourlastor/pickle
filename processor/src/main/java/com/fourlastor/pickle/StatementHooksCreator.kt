package com.fourlastor.pickle

import cucumber.api.java.After
import cucumber.api.java.Before
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement

class StatementHooksCreator(private val roundEnv: RoundEnvironment) {
    fun createBeforeHooks(methodStatements: List<TestMethodStatement>): Set<TestMethodStatement> =
            generateStatementsFor(Before::class.java, roundEnv, methodStatements)

    fun createAfterHooks(methodStatements: List<TestMethodStatement>): Set<TestMethodStatement> =
            generateStatementsFor(After::class.java, roundEnv, methodStatements)

    private fun generateStatementsFor(
            annotation: Class<out Annotation>,
            roundEnv: RoundEnvironment,
            methodStatements: List<TestMethodStatement>
    ): Set<TestMethodStatement> {
        val methods = roundEnv.getMethodsAnnotatedWith(annotation)

        return methodStatements.flatMap { (field) ->
            methods.filter { method -> field.type.toString() == method.enclosingElement.asType().toString() }
                    .map { method -> testMethodStatement(field, "\$N.\$N()", method.simpleName) }
        }.toSet()
    }
}

private fun <T : Annotation> RoundEnvironment.getMethodsAnnotatedWith(clazz: Class<T>): List<ExecutableElement> {
    return getElementsAnnotatedWith(clazz)
            .filter { it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
}
