package com.example.mpp

import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.http.HttpClientConfig
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkResponseTypedSerializer
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.http.VkKtorHttpClient
import io.ktor.client.engine.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

/**
 * Sample presenter in common code
 */
class SampleCommonPresenter {
    private val dispatchersProvider = DispatchersProvider()
    private val json = Json { encodeDefaults = false; ignoreUnknownKeys = true }

    // Create
    // Note: For now, ktor does not support background threads:
    // https://github.com/Kotlin/kotlinx.coroutines/issues/1889#issuecomment-606523539
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
            // Read more: https://github.com/Kotlin/kotlinx.coroutines/issues/1889#issuecomment-606523539
            backgroundDispatcher = dispatchersProvider.default
        )
    )

    /**
     * Get info about Pashka
     */
    @ExperimentalSerializationApi
    suspend fun getPashkaProfile(): VkUser? =
        vkApiClient.get("users.get", paramsOf("user_id" to 1))
            .let {
                json.decodeFromJsonElement(VkResponseTypedSerializer(ListSerializer(VkUser.serializer())), it)
                    .response?.firstOrNull()
            }
}

/**
 * Custom http client, based on ktor
 */
class MyHttpClient(override val coroutineContext: CoroutineContext) : VkKtorHttpClient(coroutineContext) {
    override fun createEngineWithConfig(config: HttpClientConfig): HttpClientEngine =
        HttpClientEngineProvider().httpEngine()
}

/**
 * At least for the iOS, dispatchers must have actual implementation.
 * For now, ktor does not support background threads:
 * https://github.com/Kotlin/kotlinx.coroutines/issues/1889#issuecomment-606523539
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