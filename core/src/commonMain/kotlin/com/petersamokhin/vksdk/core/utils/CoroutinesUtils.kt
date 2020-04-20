package com.petersamokhin.vksdk.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Runs a new coroutine and **blocks** the current thread _interruptibly_ until its completion.
 * There is no implementation for JS, so also no implementation in the common module.
 *
 * @see [https://github.com/Kotlin/kotlinx.coroutines/issues/672]
 *
 * @param context Coroutine context
 * @param block Block to execute
 */
expect fun <T> runBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): T