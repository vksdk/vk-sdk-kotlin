package com.petersamokhin.vksdk.internal.co.touchlab.stately.isolate

import java.util.concurrent.Callable
import java.util.concurrent.Executors

actual class BackgroundStateRunner : StateRunner {
    internal val stateExecutor = Executors.newSingleThreadExecutor()

    actual override fun <R> stateRun(block: () -> R): R {
        val result = stateExecutor.submit(Callable<RunResult> {
            try {
                Ok(block())
            } catch (e: Throwable) {
                Thrown(e)
            }
        }).get()

        @Suppress("UNCHECKED_CAST")
        return when(result){
            is Ok<*> -> result.result as R
            is Thrown -> throw result.throwable
        }
    }
}