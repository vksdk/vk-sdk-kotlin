package com.example.vkbot

import com.petersamokhin.vksdk.core.api.BatchRequestItem
import com.petersamokhin.vksdk.core.api.VkRequest
import com.petersamokhin.vksdk.core.api.botslongpoll.VkBotsLongPollApi
import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.io.FileOnDisk
import com.petersamokhin.vksdk.core.model.VkResponse
import com.petersamokhin.vksdk.core.model.VkResponseTypedSerializer
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.core.model.objects.UploadableContent
import com.petersamokhin.vksdk.core.model.objects.keyboard
import com.petersamokhin.vksdk.http.VkKtorHttpClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.logging.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import kotlin.coroutines.CoroutineContext

// will not compile intentionally, replace with yours
private const val DUMMY_USER_ID = 12345678901

class Bot : CoroutineScope {
    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    /**
     * Create your own data class for necessary requests.
     * I thought it won't be a good idea to embed in library few hundreds of data classes.
     * Keep it simple.
     */
    @Serializable
    data class VkUser(
        val id: Int,
        @SerialName("first_name")
        val firstName: String,
        @SerialName("last_name")
        val lastName: String
    )

    /**
     * Change the peer_id in the all places!
     *
     * @param clientId Group ID
     * @param accessToken Group `access_token`
     */
    @OptIn(FlowPreview::class)
    fun start(clientId: Int, accessToken: String) {
        if (accessToken == "abcdef123456...") throw RuntimeException("Please, replace dummy access_token with yours in Launcher.kt")

        // Custom implementation of the cross-platform HTTP client
        val httpClient = VkKtorHttpClient(coroutineContext, overrideClient = HttpClient(CIO) {
            engine {
                // because of long polling:
                // in case of zero events, requests will last 25 sec each
                requestTimeout = 30_000
            }
            Logging {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
            }
        })

        // OkHttp client is available only for JVM
        // val httpClient = VkOkHttpClient()

        // init settings, most of them have default values
        val settings = VkSettings(
            httpClient,
            defaultParams = paramsOf("lang" to "en")
        )

        // Client is initiated.
        // Now you can call VK API methods.
        // To use Bots Long Poll API, call `startLongPolling` asynchronously or at the end of the method.
        val client = VkApiClient(clientId, accessToken, VkApiClient.Type.Community, settings)

        // For example, get info about Pashka Durov
        // val pashkaRequest = client.call("users.get", paramsOf("user_id" to 1))

        // API rate limits? Ha-ha, we know about the `execute` method.
        // batchRequestsExample(client)

        // Send simple messages
        // simpleMessageExample(client)

        // Use the keyboard DSL
        // keyboardDslExample(client)

        // Attach image to your message
        // attachImageToMessageExample(client)

        // Attach any file to your message, or upload it and use somewhere else
        // customAttachmentExample(client)

        // You can listen for the Bots Long Poll API events
        // using asynchronous callbacks or flows.
        chatBotExample(client)

        // And, of course, for the chat bot be working,
        // we must start the long polling loop.
        // Do it at the end of your method,
        // or call in the background thread or coroutine.
        runBlocking { client.startLongPolling(settings = VkBotsLongPollApi.Settings(maxFails = 5)) }
    }

    /**
     * Call using a [VkRequest]
     */
    private suspend fun apiCallExample(pashkaRequest: VkRequest) {
        val pashkaResponseString = pashkaRequest.execute()
        val pashkaUser = parseUsersResponseFromString(pashkaResponseString)
        println("Pashka's full name is '${pashkaUser.firstName} ${pashkaUser.lastName}'")
    }

    /**
     * Use batch requests, https://vk.com/dev/execute
     *
     *  Important note: when you are use batch requests, all responses are unwrapped, e.g. `YourClass`,
     * otherwise you will get the `VkResponse<YourClass>`.
     * In our case, with `batch = true` we will get `List<VkUser>` as the response,
     * but with `batch = false`, the `VkResponse<List<VkUser>>` is returned.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun batchRequestsExample(client: VkApiClient) = launch {
        client.get(
            (1..200).map { index -> // ha-ha, 200 requests requests go brr
                BatchRequestItem(
                    request = client.call("users.get", paramsOf("user_id" to index)),
                    callback = Callback.assemble(onResult = { result ->
                        val user = parseUsersResponseFromJsonElement(result)
                        println("[result] user with id $index: $user")
                    }, onError = { error ->
                        println("[error] requesting user with id $index:")
                        error.printStackTrace()
                    })
                )
            }
        )

        // callback above handles the result already,
        // but you also can use flow
        client.flows().onBatchRequestResult()
            .collect { (request, response) ->
                // println("[batch] request: ${request}, response: $response")
            }
    }

    /**
     * Send message
     */
    private suspend fun simpleMessageExample(client: VkApiClient) {
        client.sendMessage {
            peerId = DUMMY_USER_ID
            message = "Hello, World!"

            // You can use stickers, replies, location, etc.
            // All of the message parameters are supported.
        }.execute()
    }

    /**
     * Build keyboard using convenient DSL
     */
    private suspend fun keyboardDslExample(client: VkApiClient) {
        client.sendMessage {
            peerId = DUMMY_USER_ID
            message = "You take the blue pill — the story ends..."

            keyboard = keyboard(oneTime = true) {
                row {
                    primaryButton("Blue pill")
                    negativeButton("Red pill")
                }
            }
        }.execute()
    }

    /**
     * Upload some photo and attach it to the message
     */
    private suspend fun attachImageToMessageExample(client: VkApiClient) {
        val necessaryPeerId = DUMMY_USER_ID

        val imageAttachmentString = client.uploader().uploadPhotoForMessage(
            necessaryPeerId,
            FileOnDisk("/Users/petersamokhin/Desktop/test.png")
        )

        client.sendMessage {
            peerId = necessaryPeerId
            attachment = imageAttachmentString
        }.execute()
    }

    /**
     * Upload whatever you want using the same scheme.
     * I don't know, why vtentakle can't do it by their API.
     */
    private suspend fun customAttachmentExample(client: VkApiClient) {
        val necessaryPeerId = DUMMY_USER_ID
        val attachmentType = "audio_message"

        val audioMessageAttachmentString = client.uploader().uploadContent(
            "docs.getMessagesUploadServer",
            "docs.save",
            paramsOf("type" to attachmentType, "peer_id" to necessaryPeerId),
            listOf(
                UploadableContent.File(
                    fieldName = "file",
                    fileName = "doesn't matter, but VK API is not stable; fileName must not be empty",
                    mediaType = "also doesn't matter",
                    file = FileOnDisk("/Users/petersamokhin/Desktop/test.mp3")
                )
            )
        ).let {
            retrieveAttachment(it, attachmentType)
        }

        client.sendMessage {
            peerId = necessaryPeerId
            attachment = "doc${audioMessageAttachmentString}"
        }.execute()
    }

    /**
     * Yes, it's ugly. Horrible.
     * Of course, you can use your own data class and deserialize it.
     */
    private fun retrieveAttachment(response: String, @Suppress("SameParameterValue") type: String): String? =
        json.parseToJsonElement(response)
            .jsonObjectOrNullSafe
            ?.get("response")
            ?.jsonObjectOrNullSafe
            ?.get(type)
            ?.jsonObjectOrNullSafe
            ?.let {
                "${it["owner_id"]?.contentOrNullSafe}_${it["id"]?.contentOrNullSafe}_${it["access_key"]?.contentOrNullSafe}"
            }

    /**
     * Simple chat bot which wants to work only in group chats
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun chatBotExample(client: VkApiClient) = launch {
        client.flows().onMessage()
            .flowOn(Dispatchers.IO)
            .map { (msg) ->
                val request = if (msg.isFromChat()) {
                    client.sendMessage {
                        peerId = msg.peerId
                        message = "Hello, chat"
                    }
                } else {
                    client.sendMessage {
                        peerId = msg.peerId
                        message = "Sorry, I ignore personal conversations."
                    }
                }

                client.get(BatchRequestItem(request, Callback.empty()))
            }
            .collect()
    }

    /**
     * JSON parsing is boring, and in case of the generic fields, also not trivial.
     * To provide the same convenient library API for Java and Kotlin on all platforms,
     * all inline calls are dropped and you should do it by yourself.
     */
    @OptIn(ExperimentalSerializationApi::class)
    private fun parseUsersResponseFromString(pashkaResponseString: String): VkUser {
        val pashokParsedResponse: VkResponse<List<VkUser>> = json.decodeFromString(
            VkResponseTypedSerializer(ListSerializer(VkUser.serializer())),
            pashkaResponseString
        )

        // Retrieve and return parsed data
        return pashokParsedResponse.response?.first() ?: throw Exception("Pashka? Where are you?")
    }

    private fun parseUsersResponseFromJsonElement(pashkaJsonElement: JsonElement): VkUser =
        json.decodeFromJsonElement(ListSerializer(VkUser.serializer()), pashkaJsonElement).first()
}

/**
 * Get wrapped value or null without exceptions
 */
private val JsonElement.jsonObjectOrNullSafe
    get() = try {
        jsonObject
    } catch (e: IllegalArgumentException) {
        null
    }

/**
 * Get wrapped value or null without exceptions
 */
private val JsonElement.contentOrNullSafe
    get() = try {
        jsonPrimitive.contentOrNull
    } catch (e: IllegalArgumentException) {
        null
    }