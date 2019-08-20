package com.fourlastor.pickle

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec

fun createClass(className: String, init: TypeSpec.Builder.() -> Unit): TypeSpec {
    val builder = TypeSpec.classBuilder(className)
    builder.init()
    return builder.build()
}

fun TypeSpec.Builder.method(methodName: String, init: MethodSpec.Builder.() -> Unit) {
    val builder = MethodSpec.methodBuilder(methodName)
    builder.init()
    addMethod(builder.build())
}

fun MethodSpec.Builder.code(init: CodeBlock.Builder.() -> Unit) {
    val builder = CodeBlock.builder()
    builder.init()
    addCode(builder.build())
}
