package com.fourlastor.pickle

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment

class ClassWriter(
        private val processingEnv: ProcessingEnvironment,
        private val packageName: String
) {
    fun write(typeSpec: TypeSpec) {
        val sourceFile = processingEnv.filer.createSourceFile("$packageName.${typeSpec.name}")

        sourceFile.openWriter().use {
            JavaFile.builder(packageName, typeSpec)
                    .build()
                    .writeTo(it)
        }
    }
}
