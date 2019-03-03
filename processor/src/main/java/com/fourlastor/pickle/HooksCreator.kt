package com.fourlastor.pickle

import com.squareup.javapoet.ClassName

class HooksCreator(private val statementHooksCreator: StatementHooksCreator) {

    fun create(): List<HookMethod> {
        val before = beforeHookMethod(statementHooksCreator.createBeforeHooks())
        val after = afterHookMethod(statementHooksCreator.createAfterHooks())

        return listOf(before, after)
    }

    private fun beforeHookMethod(statements: List<TestMethodStatement>) =
            HookMethod(ClassName.bestGuess("org.junit.Before")!!, "setUp", statements)

    private fun afterHookMethod(statements: List<TestMethodStatement>) =
            HookMethod(ClassName.bestGuess("org.junit.After")!!, "tearDown", statements)

}
