package com.example.vkbot

import com.petersamokhin.vksdk.core.http.HttpClientConfig
import com.petersamokhin.vksdk.http.VkKtorHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Example implementation of the HTTP client based on ktor.
 *
 * @param config Basic configurations
 */
class CioKtorHttpClient(config: HttpClientConfig = HttpClientConfig()): VkKtorHttpClient(
    config, Dispatchers.IO + SupervisorJob()
) {

    /**
     * Instantiate desired client and apply basic configurations
     *
     * @param config Basic configurations
     * @return Desired HTTP client engine, e.g. CIO, etc.
     */
    @OptIn(KtorExperimentalAPI::class)
    override fun createEngineWithConfig(config: HttpClientConfig): HttpClientEngine {
        return CIO.create {
            endpoint {
                connectTimeout = config.connectTimeout.toLong()
                requestTimeout = config.readTimeout.toLong()
            }
        }
    }
}