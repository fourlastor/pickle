package com.fourlastor.pickle

import io.cucumber.java.After
import io.cucumber.java.Before
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

@Suppress("TYPEALIAS_EXPANSION_DEPRECATION")
class StatementHooksCreator(private val roundEnv: RoundEnvironment) {
    fun createBeforeHooks(): List<TestMethodStatement> =
        generateStatementsFor(Before::class.java, roundEnv) +
                generateStatementsFor(DeprecatedBefore::class.java, roundEnv)

    fun createAfterHooks(): List<TestMethodStatement> =
        generateStatementsFor(After::class.java, roundEnv) +
                generateStatementsFor(DeprecatedAfter::class.java, roundEnv)

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
            is DeprecatedBefore -> annotation.order
            is After -> annotation.order
            is DeprecatedAfter -> annotation.order
            else -> throw PickleException("Unexpected Cucumber annotation used: $annotationType")
        }
}
