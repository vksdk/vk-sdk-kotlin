package com.petersamokhin.vksdk.core.callback

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Callback for network calls
 */
public interface Callback<in R : Any> {
    /**
     * Called when the network call is successful
     *
     * @param result Result of a call
     */
    public fun onResult(result: R)

    /**
     * Called when the network call failed
     *
     * @param error Some error
     */
    public fun onError(error: Throwable)

    public companion object {
        @JvmStatic
        public fun <T : Any> empty(): Callback<T> =
            object : Callback<T> {
                override fun onResult(result: T) = Unit
                override fun onError(error: Throwable) = Unit
            }

        @JvmStatic
        @JvmOverloads
        public fun <T : Any> assemble(
            onResult: SimpleCallback<T> = SimpleCallback.empty(),
            onError: SimpleCallback<Throwable> = SimpleCallback.empty()
        ): Callback<T> =
            object : Callback<T> {
                override fun onResult(result: T) = onResult.accept(result)
                override fun onError(error: Throwable) = onError.accept(error)
            }
    }
}