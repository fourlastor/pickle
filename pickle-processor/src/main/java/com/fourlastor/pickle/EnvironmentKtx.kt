package com.fourlastor.pickle

import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement

internal fun <T : Annotation> RoundEnvironment.getMethodsAnnotatedWith(clazz: Class<T>): List<ExecutableElement> {
    return getElementsAnnotatedWith(clazz)
            .filter { it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
}
