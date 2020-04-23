package com.example.mpp

import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.error.VkException
import com.petersamokhin.vksdk.core.http.HttpClientConfig
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkResponseTypedSerializer
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.core.utils.runBlocking
import com.petersamokhin.vksdk.http.VkKtorHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.SharedImmutable
import kotlin.native.concurrent.ThreadLocal

/**
 * Sample presenter in common code
 */
class SampleCommonPresenter {
    private val dispatchersProvider = DispatchersProvider()
    private val json = Json(JsonConfiguration.Stable.copy(encodeDefaults = false, ignoreUnknownKeys = true))

    // Create
    // Note: For now, ktor does not support background threads: https://github.com/ktorio/ktor/issues/1538
    // See the actual class DispatchersProvider in the iosMain module.
    private val vkHttpClient = MyHttpClient(dispatchersProvider.default)

    // Init the client
    private val vkApiClient = VkApiClient(
        id = 151083290,
        token = "abcdef123456...",
        type = VkApiClient.Type.Community,
        settings = VkSettings(
            vkHttpClient,
            maxExecuteRequestsPerSecond = 3,
            // Under the hood, this dispatcher is also used for ktor network calls.
            // So, for now, unfortunately, we should provide `main` thread dispatcher.
            // See the actual class DispatchersProvider in the iosMain module.
            // Read more: https://github.com/ktorio/ktor/issues/1538
            backgroundDispatcher = dispatchersProvider.default
        )
    )

    /**
     * Get info about Pashka
     */
    fun getPashkaProfile(): Flow<VkUser> {
        return vkApiClient.flows()
            .call("users.get", paramsOf("user_id" to 1), batch =true)
            .map {
                // only the ListSerializer, because VK's execute method returning raw responses array
                json.fromJson(ListSerializer(VkUser.serializer()), it).first()
            }
    }

    /**
     * Get info about Pashka
     */
    fun getPashkaProfileAsync(block: (VkUser) -> Unit) {
        vkApiClient.call("users.get", paramsOf("user_id" to 1))
            .enqueue(onResult = {
                // but here call is not executed in batch queue, so you should unwrap it
                json.parse(VkResponseTypedSerializer(ListSerializer(VkUser.serializer())), it)
                    .also {
                        if (it.error != null) {
                            showError()
                        }
                    }
                    .response // it can be null
                    ?.first()
                    ?.also(block) ?: showError()
            }, onError = ::showError)
    }

    /**
     * Handle the error
     */
    private fun showError(error: Exception? = null) {}

}

/**
 * Custom http client, based on ktor
 */
class MyHttpClient(override val coroutineContext: CoroutineContext) : VkKtorHttpClient() {
    override fun createEngineWithConfig(config: HttpClientConfig): HttpClientEngine = HttpClientEngineProvider().httpEngine()
}

/**
 * At least for the iOS, dispatchers must have actual implementation.
 * For now, ktor does not support background threads: https://github.com/ktorio/ktor/issues/1538
 */
expect class DispatchersProvider() {
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
}

/**
 * Actual engine is platform-based
 */
expect class HttpClientEngineProvider() {
    fun httpEngine(): HttpClientEngine
}

@Serializable
data class VkUser(
    val id: Int,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String
)