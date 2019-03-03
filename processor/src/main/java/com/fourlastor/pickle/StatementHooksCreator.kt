package com.fourlastor.pickle

import cucumber.api.java.After
import cucumber.api.java.Before
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

class StatementHooksCreator(private val roundEnv: RoundEnvironment) {
    fun createBeforeHooks(): List<TestMethodStatement> =
            generateStatementsFor(Before::class.java, roundEnv)

    fun createAfterHooks(): List<TestMethodStatement> =
            generateStatementsFor(After::class.java, roundEnv)

    private fun generateStatementsFor(
            annotationType: Class<out Annotation>,
            roundEnv: RoundEnvironment
    ): List<TestMethodStatement> {
        val methods = roundEnv.getMethodsAnnotatedWith(annotationType)

        return methods.sortedBy {
            it.annotationOrder(annotationType)
        }.map { method ->
            val field = TestField(method.enclosingElement as TypeElement)
            testMethodStatement(field, "\$N.\$N()", method.simpleName)
        }
    }

    private fun ExecutableElement.annotationOrder(annotationType: Class<out Annotation>) =
            when (val annotation = getAnnotation(annotationType)) {
                is Before -> annotation.order
                is After -> annotation.order
                else -> DEFAULT_CUCUMBER_HOOK_ORDER
            }
}

private const val DEFAULT_CUCUMBER_HOOK_ORDER = 10000

private fun <T : Annotation> RoundEnvironment.getMethodsAnnotatedWith(clazz: Class<T>): List<ExecutableElement> {
    return getElementsAnnotatedWith(clazz)
            .filter { it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
}
