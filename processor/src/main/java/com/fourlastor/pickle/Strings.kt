package com.fourlastor.pickle

val nonAlphaNumericChar = Regex("\\W")

fun String.toCamelCase() = split(" ")
        .joinToString(separator = "") { it.capitalize() }
        .replace(nonAlphaNumericChar, "")
