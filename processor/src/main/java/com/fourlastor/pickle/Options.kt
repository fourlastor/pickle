package com.fourlastor.pickle

import javax.annotation.processing.RoundEnvironment

data class Options(val featuresDirPath: String, val packageName: String, val strictMode: Boolean)

fun options(roundEnvironment: RoundEnvironment): Options {
    val options = roundEnvironment.getElementsAnnotatedWith(Pickle::class.java)
    return options.first().getAnnotation(Pickle::class.java).run {
        Options(
                featuresDir,
                packageName,
                strictMode
        )
    }
}
