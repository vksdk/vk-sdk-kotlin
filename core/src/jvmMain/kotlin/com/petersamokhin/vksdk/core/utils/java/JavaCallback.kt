package com.petersamokhin.vksdk.core.utils.java

import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.callback.SimpleCallback
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Useful for calling suspend functions from Java
 */
public abstract class SuspendCallback<T : Any> : Continuation<T>, Callback<T> {
    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<T>) {
        result.getOrNull()?.also(::onResult)
        result.exceptionOrNull()?.also(::onError)
    }

    public companion object {
        @JvmStatic
        public fun <T : Any> empty(): SuspendCallback<T> =
            object : SuspendCallback<T>() {
                override fun onResult(result: T) = Unit
                override fun onError(error: Throwable) = Unit
            }

        @JvmStatic
        @JvmOverloads
        public fun <T : Any> assemble(
            onResult: SimpleCallback<T> = SimpleCallback.empty(),
            onError: SimpleCallback<Throwable> = SimpleCallback.empty()
        ): SuspendCallback<T> =
            object : SuspendCallback<T>() {
                override fun onResult(result: T) = onResult.accept(result)
                override fun onError(error: Throwable) = onError.accept(error)
            }
    }
}