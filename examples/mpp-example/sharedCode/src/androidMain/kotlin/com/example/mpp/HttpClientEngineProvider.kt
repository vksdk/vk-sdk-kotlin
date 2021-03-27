package com.example.mpp

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Actual engine is platform-based
 */
actual class HttpClientEngineProvider actual constructor() {
    actual fun httpEngine(): HttpClientEngine = Android.create()
}

actual class DispatchersProvider actual constructor() {
    actual val main: CoroutineDispatcher
        get() = Dispatchers.Main

    actual val default: CoroutineDispatcher
        get() = Dispatchers.Default
}