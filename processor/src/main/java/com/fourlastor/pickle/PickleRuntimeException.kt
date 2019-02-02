package com.fourlastor.pickle

open class PickleRuntimeException(message: String) : RuntimeException("""
    Pickle Error:
    $message
""".trimIndent())
