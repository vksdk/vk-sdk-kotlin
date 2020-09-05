package com.example.vkbot

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
import com.petersamokhin.vksdk.http.VkOkHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.logging.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
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
    @OptIn(KtorExperimentalAPI::class)
    fun start(clientId: Int, accessToken: String) {
        if (accessToken == "abcdef123456...") throw RuntimeException("Please, replace dummy access_token with yours in Launcher.kt")

        // Custom implementation of the cross-platform HTTP client
        val httpClient = VkKtorHttpClient(coroutineContext, overrideClient = HttpClient(CIO) {
            engine {
                // because of long polling
                requestTimeout = 30000
            }
            Logging {
                level = LogLevel.BODY
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

        // see the sync API call example:
        // syncApiCallExample(pashkaRequest)

        // Or async:
        // asyncApiCallExample(pashkaRequest)

        // Or use [Flow]
        // flowApiCallExample(client, pashkaRequest)

        // API rate limits? Ha-ha, we know about the `execute` method.
        //
        // Important note: when you are use batch requests, all responses are unwrapped, e.g. `YourClass`,
        // otherwise you will get the `VkResponse<YourClass>`.
        // In our case, with `batch = true` we will get `List<VkUser>` as the response,
        // but with `batch = false`, the `VkResponse<List<VkUser>>` is returned.
        // executeBatchRequestExample(client, pashkaRequest)

        // Send simple messages
        // Of course, you can use sync, async, flow ways
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

        // And, of course, to chat bot be working,
        // we must start the long polling loop.
        // Do it at the end of your method,
        // or call in the background thread or coroutine.
        client.startLongPolling(settings = VkBotsLongPollApi.Settings(maxFails = 5))
    }

    /**
     * Use synchronous calls to the API
     */
    private fun syncApiCallExample(pashkaRequest: VkRequest) {
        val pashkaResponseString = pashkaRequest.execute()
        val pashkaUserSync = parseUsersResponseFromString(pashkaResponseString)
        println("Pashka's full name is '${pashkaUserSync.firstName} ${pashkaUserSync.lastName}'")
    }

    /**
     * Use asynchronous calls to the API
     */
    private fun asyncApiCallExample(pashkaRequest: VkRequest) {
        pashkaRequest.enqueue(object : Callback<String> {
            override fun onResult(result: String) {
                val pashkaUserAsync = parseUsersResponseFromString(result)

                println("Pashka's full name is '${pashkaUserAsync.firstName} ${pashkaUserAsync.lastName}'")
            }

            override fun onError(error: Exception) {
                println("Some error occurred:")
                error.printStackTrace()
            }
        })
    }

    /**
     * Use Flows with calls to the API
     */
    private fun flowApiCallExample(client: VkApiClient, pashkaRequest: VkRequest) {
        client.flows().call(pashkaRequest, batch = true)
            .map(::parseUsersResponseFromJsonElement)
            .onEach { pashkaUser ->
                println("Pashka's full name is '${pashkaUser.firstName} ${pashkaUser.lastName}'")
            }
            .flowOn(Dispatchers.IO)
            .launchIn(this)
    }

    /**
     * Use batch requests, https://vk.com/dev/execute
     */
    private fun executeBatchRequestExample(client: VkApiClient, pashkaRequest: VkRequest) {
        // ha-ha, 200 requests requests go brr
        for (i in 0 until 200) {

            // All the calls will be put into queue.
            // You can do up to `25 * maxExecuteRequestsPerSecond` (see the client settings)
            // requests per second using this feature.
            client.flows().call(pashkaRequest, batch = true)
                .map(::parseUsersResponseFromJsonElement)
                .onEach { pashkaUser ->
                    println("Pashka's full name is '${pashkaUser.firstName} ${pashkaUser.lastName}'")
                }
                .flowOn(Dispatchers.IO)
                .launchIn(this)
        }
    }

    /**
     * Send message
     */
    private fun simpleMessageExample(client: VkApiClient) {
        client.sendMessage {
            peerId = 12345678901
            message = "Hello, World!"

            // You can use stickers, replies, location, etc.
            // All of the message parameters are supported.
        }.execute()
    }

    /**
     * Build keyboard using convenient DSL
     */
    private fun keyboardDslExample(client: VkApiClient) {
        client.sendMessage {
            peerId = 12345678901
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
    private fun attachImageToMessageExample(client: VkApiClient) {
        val necessaryPeerId = 12345678901

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
    private fun customAttachmentExample(client: VkApiClient) {
        val necessaryPeerId = 12345678901
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
    private fun retrieveAttachment(response: String, @Suppress("SameParameterValue") type: String): String? {
        return json.parseToJsonElement(response)
            .jsonObjectOrNullSafe
            ?.get("response")
            ?.jsonObjectOrNullSafe
            ?.get(type)
            ?.jsonObjectOrNullSafe
            ?.let {
                "${it["owner_id"]?.contentOrNullSafe}_${it["id"]?.contentOrNullSafe}_${it["access_key"]?.contentOrNullSafe}"
            }
    }

    /**
     * Simple chat bot which wants to work only in group chats
     */
    private fun chatBotExample(client: VkApiClient) {
        client.flows().onMessage()
            .flatMapLatest {
                if (it.message.isFromChat()) {
                    client.flows().sendMessage {
                        peerId = it.message.peerId
                        message = "Hello, chat"
                    }
                } else {
                    client.flows().sendMessage {
                        peerId = it.message.peerId
                        message = "Sorry, I ignore personal conversations."
                    }
                }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(this)
    }

    /**
     * JSON parsing is boring, and in case of the generic fields, also not trivial.
     * To provide the same convenient library API for Java and Kotlin on all platforms,
     * all inline calls are dropped and you should do it by yourself.
     */
    private fun parseUsersResponseFromString(pashkaResponseString: String): VkUser {
        val pashokParsedResponse: VkResponse<List<VkUser>> = json.decodeFromString(
            VkResponseTypedSerializer(ListSerializer(VkUser.serializer())),
            pashkaResponseString
        )

        // Retrieve and return parsed data
        return pashokParsedResponse.response?.first() ?: throw Exception("Pashka? Where are you?")
    }

    @Suppress("RedundantSuspendModifier") // IDEA lint bug?
    private suspend fun parseUsersResponseFromJsonElement(pashkaJsonElement: JsonElement): VkUser {
        return json.decodeFromJsonElement(ListSerializer(VkUser.serializer()), pashkaJsonElement).first()
    }
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