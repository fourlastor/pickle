package com.fourlastor.pickle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.api.UnitTestVariant
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.*
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.lang.model.element.Modifier

class PicklePlugin : Plugin<Project> {

    open class GenerateTask : DefaultTask() {

        @Input
        lateinit var packageName: String

        @InputDirectory
        val featuresDir: DirectoryProperty = project.objects.directoryProperty()

        @Input
        var strictMode: Boolean? = null

        @OutputFile
        lateinit var hashClassFile: File

        @TaskAction
        fun generateSources() {
            val featuresDir = featuresDir.get().asFile
            val hashCodeValue = featuresDir
                    .walkTopDown()
                    .filter { !it.isDirectory && it.name.endsWith(".feature", ignoreCase = true) }
                    .fold("") { acc, file ->
                        acc + file.readText()
                    }.hashCode()

            val hashCode = FieldSpec.builder(Int::class.java, "HASH_CODE")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$L", hashCodeValue)
                    .build()

            val pickleAnnotation = AnnotationSpec.builder(Pickle::class.java)
                    .addMember("featuresDir", "\$S", featuresDir.absolutePath)
                    .addMember("packageName", "\$S", packageName)
                    .addMember("strictMode", "\$L", strictMode!!)
                    .build()

            val hashClass = TypeSpec.classBuilder("PickleHash")
                    .addAnnotation(pickleAnnotation)
                    .addModifiers(Modifier.PUBLIC)
                    .addField(hashCode)
                    .build()

            if (!hashClassFile.exists()) {
                hashClassFile.parentFile.mkdirs()
            }

            hashClassFile.writer().use {
                JavaFile.builder(packageName, hashClass)
                    .build()
                    .writeTo(it)
            }
        }
    }

    open class Extension(
            var packageName: String? = null,
            var strictMode: Boolean = true,
            var androidTest: TestExtension = TestExtension(true),
            var unitTest: TestExtension = TestExtension(false)
    ) {
        fun androidTest(action: Action<TestExtension>) = action.execute(androidTest)
        fun unitTest(action: Action<TestExtension>) = action.execute(unitTest)
    }

    open class TestExtension(var enabled: Boolean, var featuresDir: String? = null)

    override fun apply(project: Project) {
        val extension = project.extensions.create("pickle", Extension::class.java)

        project.afterEvaluate {
            project.plugins.all { plugin ->
                when (plugin) {
                    is LibraryPlugin -> {
                        project.extensions.findByType(LibraryExtension::class.java)?.run {
                            if (extension.androidTest.enabled) {
                                configureAndroidTest(project, testVariants, extension)
                            }
                            if (extension.unitTest.enabled) {
                                configureUnitTest(project, unitTestVariants, extension)
                            }
                        }
                    }
                    is AppPlugin -> {
                        project.extensions.findByType(AppExtension::class.java)?.run {
                            if (extension.androidTest.enabled) {
                                configureAndroidTest(project, testVariants, extension)
                            }
                            if (extension.unitTest.enabled) {
                                configureUnitTest(project, unitTestVariants, extension)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun configureAndroidTest(project: Project, variants: DomainObjectSet<out TestVariant>, extension: Extension) {
        val featuresDir = extension.androidTest.featuresDir
            ?: throw IllegalStateException("You must specify \"featuresDir\" inside \"androidTest\" for pickle to work with Android tests")

        configure(project, variants, extension) { variant, directoryProperty ->
            directoryProperty.set(variant.mergeAssetsProvider.flatMap { it.outputDir.dir(featuresDir) })
        }
    }

    private fun configureUnitTest(project: Project, variants: DomainObjectSet<out UnitTestVariant>, extension: Extension) {
        val featuresDir = extension.unitTest.featuresDir
            ?: throw IllegalStateException("You must specify \"featuresDir\" inside \"unitTest\" for pickle to work with unit tests")

        configure(project, variants, extension) { _, directoryProperty ->
            directoryProperty.set(File(featuresDir))
        }
    }

    private fun configure(
        project: Project,
        variants: DomainObjectSet<out BaseVariant>,
        extension: Extension,
        featureFn: (BaseVariant, DirectoryProperty) -> Unit
    ) {
        val packageName = extension.packageName ?: throw IllegalStateException("You must specify \"packageName\" for pickle")
        val strictMode = extension.strictMode

        variants.all { variant ->
            val outputDir = project.buildDir.resolve("generated/source/pickle/${variant.dirName}")
            val taskName = "generatePickleHashClass" + variant.name.capitalize()

            project.tasks.create(taskName, GenerateTask::class.java) { task ->
                task.packageName = packageName
                featureFn(variant, task.featuresDir)
                task.strictMode = strictMode
                task.hashClassFile = File(outputDir, "PickleHash.java")
                variant.registerJavaGeneratingTask(task, outputDir)
            }
        }
    }
}
