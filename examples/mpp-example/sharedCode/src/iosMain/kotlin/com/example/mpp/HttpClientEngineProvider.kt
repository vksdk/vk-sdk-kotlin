package com.example.mpp

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.ios.Ios
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Actual engine is platform-based
 */
actual class HttpClientEngineProvider actual constructor() {
    actual fun httpEngine(): HttpClientEngine = Ios.create()
}

/**
 * Special platform-specific dispatchers.
 * Must be here for iOS!
 */
actual class DispatchersProvider actual constructor() {
    actual val main: CoroutineDispatcher
        get() = Dispatchers.Main

    actual val default: CoroutineDispatcher
        get() = Dispatchers.Main // Dispatchers.Default
        // For now, ktor does not support background threads:
        // https://github.com/Kotlin/kotlinx.coroutines/issues/1889#issuecomment-606523539
        // get() = Dispatchers.Default
}