package com.fourlastor.pickle

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

class ClassGenerator {

    companion object {
        private val Test = ClassName.bestGuess("org.junit.Test")!!
        private val Ignore = ClassName.bestGuess("org.junit.Ignore")!!
        private val RunWith = ClassName.bestGuess("org.junit.runner.RunWith")
        private val AndroidJUnit4 = ClassName.bestGuess("android.support.test.runner.AndroidJUnit4")
    }

    fun generate(testClass: TestClass) = createClass(testClass.name) {
        runWithJUnit4()
        addModifiers(Modifier.PUBLIC)

        if (testClass.isAllTestsIgnored()) {
            addAnnotation(AnnotationSpec.builder(Ignore).build())
        }

        testClass.hookMethods
                .filter { it.statements.isNotEmpty() }
                .forEach { hook ->
                    method(hook.name) {
                        addModifiers(Modifier.PUBLIC)
                        addException(Throwable::class.java)
                        addAnnotation(hook.annotation)

                        code {
                            hook.statements.forEach {
                                addStatement(it.statementFormat, *it.args.toTypedArray())
                            }
                        }
                    }
                }

        testClass.methods.forEach { method ->
            method(method.name) {

                addModifiers(Modifier.PUBLIC)
                addException(Throwable::class.java)
                addAnnotation(Test)
                when (method) {
                    is IgnoredTestMethod -> {
                        addAnnotation(ignore(method.scenarioName))
                    }
                    is ImplementedTestMethod -> {
                        method.statements.forEach {
                            addStatement(it.statementFormat, *it.args.toTypedArray())
                        }
                    }
                }
            }
        }

        testClass.fields.map { it.toFieldSpec() }
                .forEach { addField(it) }
    }

    private fun TestClass.isAllTestsIgnored() = methods.all { it is IgnoredTestMethod }

    private fun ignore(scenarioName: String) = AnnotationSpec.builder(Ignore)
            .addMember("value", "\$S", "Missing steps for scenario \"$scenarioName\"")
            .build()

    private fun TypeSpec.Builder.runWithJUnit4() = addAnnotation(
            AnnotationSpec.builder(RunWith)
                    .addMember("value", "\$T.class", AndroidJUnit4)
                    .build()
    )

    private fun TestField.toFieldSpec(): FieldSpec {
        val stepsClass = ClassName.get(type)

        return FieldSpec.builder(stepsClass, name)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new \$T()", stepsClass)
                .build()
    }
}

class MissingStepDefinitionException(stepName: String) : RuntimeException("Missing step definition for \"$stepName\"")
