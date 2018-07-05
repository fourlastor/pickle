package com.fourlastor.pickle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.TestVariant
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
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
        lateinit var featuresDir: File

        @Input
        var strictMode: Boolean? = null

        @OutputFile
        lateinit var hashClassFile: File

        @TaskAction
        fun generateSources() {
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

    open class Extension {
        var packageName: String? = null
        var featuresDir: String? = null
        var strictMode: Boolean = true
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create("pickle", Extension::class.java)

        project.afterEvaluate {
            project.plugins.all {
                when (it) {
                    is LibraryPlugin -> {
                        project.extensions.findByType(LibraryExtension::class.java).run {
                            configure(project, testVariants, extension)
                        }
                    }
                    is AppPlugin -> {
                        project.extensions.findByType(AppExtension::class.java).run {
                            configure(project, testVariants, extension)
                        }
                    }
                }
            }
        }
    }

    private fun configure(project: Project, variants: DomainObjectSet<out TestVariant>, extension: Extension) {
        val featuresDir = extension.featuresDir ?: throw IllegalStateException("You must specify \"featuresDir\" for pickle")
        val packageName = extension.packageName ?: throw IllegalStateException("You must specify \"packageName\" for pickle")
        val strictMode = extension.strictMode

        variants.all { variant ->
            val outputDir = project.buildDir.resolve("generated/source/pickle/${variant.dirName}")
            val taskName = "generatePickleHashClass" + variant.name.capitalize()

            val task = project.tasks.create(taskName, GenerateTask::class.java).apply {
                this.packageName = packageName
                this.featuresDir = File(variant.mergeAssets.outputDir, featuresDir)
                this.strictMode = strictMode
                this.hashClassFile = File(outputDir, "PickleHash.java")
            }

            task.dependsOn(variant.mergeAssets)
            variant.registerJavaGeneratingTask(task, outputDir)
        }
    }
}
