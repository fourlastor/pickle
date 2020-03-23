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

    private val featuresDir = featuresDir("features")
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
    fun featureWithAmbiguousDefinedStepsInStrictMode() {
        val compilation = whenCompilingWith(
            featuresDir,
            "target",
            strict,
            "steps/Steps.java",
            "steps/OtherSteps.java",
            "steps/OtherStepsWithAmbiguousStep.java"
        )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("Multiple step implementations matched.")
        assertThat(compilation).hadErrorContaining("> Step definition: \"A step with 1 as parameter\"")
        assertThat(compilation).hadErrorContaining("> Step implementation with regexes:")
        assertThat(compilation).hadErrorContaining("^A step with (\\w+) as parameter$")
        assertThat(compilation).hadErrorContaining("^A step with (\\w?) as parameter$")
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
        assertThat(compilation).hadWarningContaining("Missing step definition for \"A step from another definition file\"")
        assertThat(compilation).hadWarningContaining("\"Scenario: Scenario with one step and background\" will be skipped.")
        assertThat(compilation).successfullyGeneratedTestClasses(
            "$packageName/AFeatureWithoutBackgroundTest.java"
        )
    }

    @Test
    fun featureWithAllDefinedStepsMissingInNonStrictMode() {
        val packageName = "targetForNonStrictModeAllStepsMissing"

        val compilation = whenCompilingWith(
            featuresDir,
            packageName,
            nonStrict
        )

        assertThat(compilation).succeeded()
        assertThat(compilation).hadWarningContaining("Missing step definition for \"A step without parameters\"")
        assertThat(compilation).hadWarningContaining("Missing step definition for \"A step with 1 as parameter\"")
        assertThat(compilation).hadWarningContaining("Missing step definition for \"A step with 2 as parameter\"")
        assertThat(compilation).hadWarningContaining("Missing step definition for \"A step without parameters\"")
        assertThat(compilation).hadWarningContaining("Missing step definition for \"A step with 1 as parameter\"")
        assertThat(compilation).hadWarningContaining("Missing step definition for \"A step with a as parameter\"")
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

    @Test
    fun featureWithDefinedStepsAndHooksFromSeparateFile() {
        val packageName = "targetWithSeparateHooks"
        val compilation = whenCompilingWith(
            featuresDir,
            packageName,
            strict,
            "steps/Steps.java", "steps/OtherSteps.java", "steps/JustHooks.java"
        )

        assertThat(compilation).successfullyGeneratedTestClasses(
            "$packageName/AFeatureWithoutBackgroundTest.java",
            "$packageName/AFeatureWithBackgroundTest.java"
        )
    }

    @Test
    fun featureWithDuplicateScenarios() {
        val compilation = whenCompilingWith(
            featuresDir("featuresDuplicateScenario"),
            "target",
            strict,
            "steps/Steps.java"
        )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("Scenarios need to have unique names.")
        assertThat(compilation).hadErrorContaining("Duplicate scenarios:")
        assertThat(compilation).hadErrorContaining("> Duplicate scenario")
    }

    @Test
    fun featureWithStepDefinitionArgumentsMismatch() {
        val compilation = whenCompilingWith(
            featuresDir,
            "target",
            strict,
            "steps/Steps.java", "steps/OtherStepsWithWrongArguments.java"
        )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("Step definition argument mismatch.")
        assertThat(compilation).hadErrorContaining("> Step definition: \"A step from another definition file\"")
        assertThat(compilation).hadErrorContaining("> Step implementation: steps.OtherStepsWithWrongArguments.aStepFromAnotherDefinitionFile(java.lang.String)")
    }

    @Test
    fun deprecatedAnnotationsSuppoorted() {
        val packageName = "targetDeprecated"

        val compilation = whenCompilingWith(
            featuresDir,
            packageName,
            strict,
            "steps/DeprecatedSteps.java"
        )

        assertThat(compilation).successfullyGeneratedTestClasses(
            "$packageName/AFeatureWithoutBackgroundTest.java",
            "$packageName/AFeatureWithBackgroundTest.java"
        )
    }

    private fun featuresDir(dirName: String) = Resources.getResource(dirName).path

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
        }.asJavaFileObject("androidx.test.ext.junit.runners")

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
