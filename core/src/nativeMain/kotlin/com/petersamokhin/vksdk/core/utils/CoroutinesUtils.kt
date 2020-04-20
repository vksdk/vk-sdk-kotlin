package com.petersamokhin.vksdk.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Runs a new coroutine and **blocks** the current thread _interruptibly_ until its completion.
 * There is no implementation for JS, so also no implementation in the common module.
 *
 * @see [https://github.com/Kotlin/kotlinx.coroutines/issues/672]
 *
 * @param context Coroutine context
 * @param block Block to execute
 */
actual fun <T> runBlocking(context: CoroutineContext, block: suspend CoroutineScope.() -> T): T {
    return kotlinx.coroutines.runBlocking(context, block)
}