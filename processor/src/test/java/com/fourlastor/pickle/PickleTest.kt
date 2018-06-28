package com.fourlastor.pickle

import com.google.common.io.Resources
import com.google.testing.compile.Compilation
import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.junit.Test
import org.junit.runner.Runner
import javax.lang.model.element.Modifier
import javax.tools.JavaFileObject

class PickleTest {

    private val featuresDir = Resources.getResource("features").path
    private val strict = true
    private val nonStrict = false

    @Test
    fun featureWithDefinedSteps() {
        val packageName = "target"

        val compilation = whenCompilingWith(
                featuresDir,
                packageName,
                strict,
                "steps/Steps.java", "steps/OtherSteps.java"
        )

        assertThat(compilation).successfullyGeneratedTestClasses(
                "$packageName/AFeatureWithoutBackgroundTest.java",
                "$packageName/AFeatureWithBackgroundTest.java"
        )
    }

    @Test
    fun featureWithMissingDefinedStepsInStrictMode() {
        val compilation = whenCompilingWith(
                featuresDir,
                "target",
                strict,
                "steps/Steps.java"
        )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("Missing step definition for \"A step from another definition file\"")
    }

    @Test
    fun featureWithMissingDefinedStepsInNonStrictMode() {
        val packageName = "targetForNonStrictMode"

        val compilation = whenCompilingWith(
                featuresDir,
                packageName,
                nonStrict,
                "steps/Steps.java"
        )

        assertThat(compilation).succeeded()
        assertThat(compilation).successfullyGeneratedTestClasses(
                "$packageName/AFeatureWithoutBackgroundTest.java"
        )
    }


    @Test
    fun featureWithDefinedStepsAndHooks() {
        val packageName = "targetWithHooks"
        val compilation = whenCompilingWith(
                featuresDir,
                packageName,
                strict,
                "steps/Steps.java", "steps/OtherStepsWithHooks.java"
        )

        assertThat(compilation).successfullyGeneratedTestClasses(
                "$packageName/AFeatureWithoutBackgroundTest.java",
                "$packageName/AFeatureWithBackgroundTest.java"
        )
    }

    private fun whenCompilingWith(
            featuresDir: String,
            targetPackageName: String,
            strictMode: Boolean,
            vararg availableSteps: String
    ): Compilation {
        val stepsDefinitions = availableSteps
                .map { JavaFileObjects.forResource(it) }
                .toTypedArray()

        return javac()
                .withProcessors(PickleProcessor())
                .compile(
                        pickleConfiguration(featuresDir, targetPackageName, strictMode),
                        androidJUnit4(),
                        *stepsDefinitions
                )
    }

    private fun pickleConfiguration(featuresDir: String, packageName: String, strictMode: Boolean): JavaFileObject {
        return createClass("PickleHash") {
            addAnnotation(
                    AnnotationSpec.builder(Pickle::class.java)
                            .addMember("featuresDir", "\$S", featuresDir)
                            .addMember("packageName", "\$S", packageName)
                            .addMember("strictMode", "\$L", strictMode)
                            .build()
            )

            addField(
                    FieldSpec.builder(Int::class.java, "HASH_CODE", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                            .initializer("\$L", 123)
                            .build()
            )
        }.asJavaFileObject()
    }

    private fun androidJUnit4(): JavaFileObject =
            createClass("AndroidJUnit4") {
                superclass(Runner::class.java)
                addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            }.asJavaFileObject("android.support.test.runner")

    private fun TypeSpec.asJavaFileObject(packageName: String = "com.example"): JavaFileObject {
        val javaFile = JavaFile.builder(packageName, this)
                .build()

        return JavaFileObjects.forSourceString("${javaFile.packageName}.$name", javaFile.toString())
    }

    private fun CompilationSubject.successfullyGeneratedTestClasses(vararg testClasses: String) {
        succeeded()

        testClasses.forEach { testClass ->
            val fullyQualifiedName = testClass.removeSuffix(".java")
                    .replace('/', '.')

            val testClassSource = JavaFileObjects.forResource(testClass)

            generatedSourceFile(fullyQualifiedName)
                    .hasSourceEquivalentTo(testClassSource)
        }
    }
}
