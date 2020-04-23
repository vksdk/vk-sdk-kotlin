package com.petersamokhin.vksdk.core.utils

import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.callback.EventCallback

/**
 * `true` -> `1`, `false` -> `0`
 */
internal inline val Boolean.intValue
    get() = if (this) 1 else 0

/**
 * Make callback from two lambdas
 *
 * @return Callback object
 */
internal inline fun <R: Any> assembleCallback(
    crossinline onResult: (R) -> Unit,
    crossinline onError: (Exception) -> Unit
): Callback<R> {
    return object : Callback<R> {
        override fun onResult(result: R) = onResult.invoke(result)
        override fun onError(error: Exception) = onError.invoke(error)
    }
}

/**
 * Make callback from lambda
 *
 * @return Callback object
 */
internal inline fun <R: Any> assembleEventCallback(
    crossinline block: (R) -> Unit
): EventCallback<R> {
    return object : EventCallback<R> {
        override fun onEvent(event: R) = block.invoke(event)
    }
}