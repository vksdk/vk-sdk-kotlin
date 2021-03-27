package com.petersamokhin.vksdk.core.api.botslongpoll

import com.petersamokhin.vksdk.core.api.VkApi
import com.petersamokhin.vksdk.core.api.botslongpoll.VkBotsLongPollApi.Settings.Companion.IGNORE_FAILS
import com.petersamokhin.vksdk.core.callback.EventCallback
import com.petersamokhin.vksdk.core.error.VkException
import com.petersamokhin.vksdk.core.error.VkResponseException
import com.petersamokhin.vksdk.core.error.VkSdkInitiationException
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkLongPollServerResponse
import com.petersamokhin.vksdk.core.model.VkResponse
import com.petersamokhin.vksdk.core.model.VkResponseTypedSerializer
import com.petersamokhin.vksdk.core.utils.*
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmOverloads

/**
 * Bots LongPoll API
 *
 * @property clientId LongPoll client ID, usually a community ID
 * @property api API wrapper to make network calls
 */
public class VkBotsLongPollApi @JvmOverloads constructor(
    private val clientId: Int,
    private val api: VkApi,
    private val backgroundDispatcher: CoroutineDispatcher,
    private val json: Json = defaultJson()
) : CoroutineScope {
    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("BotsLongPollApi::exceptionHandler::error = $throwable")
        throwable.printStackTrace()
    }

    /**
     * Coroutine context for jobs
     */
    override val coroutineContext: CoroutineContext
        get() = backgroundDispatcher + job + exceptionHandler

    private val updatesHandler = VkLongPollEventsHandler(json, job, backgroundDispatcher)

    /**
     * Start long polling with settings
     *
     * @param settings Long polling settings, such as wait time for server
     * @return Associated coroutine Job
     */
    public fun startPolling(settings: Settings): Job {
        if (job.isCompleted || job.isCancelled) throw VkSdkInitiationException("BotsLongPollApi")

        return launch {
            var serverInfo = getInitialServerInfo().response
                ?: throw VkResponseException("BotsLongPollApi initiation error: bad VK response (server info)")
            var lastUpdatesResponse: JsonObject? = getUpdatesResponse(serverInfo, settings.wait)
                ?: throw VkResponseException("BotsLongPollApi initiation error: bad VK response (last updates)")
            var failNum = 0

            while (job.isActive) {
                if (settings.maxFails != IGNORE_FAILS && failNum >= settings.maxFails)
                    throw VkResponseException("BotsLongPollApi keeps failing (error #$failNum), last response: $lastUpdatesResponse")

                if (lastUpdatesResponse == null) {
                    failNum++
                    serverInfo = getInitialServerInfo().response
                        ?: throw VkResponseException("BotsLongPollApi failed retrieving server info after error: bad VK response (server info #2)")
                    lastUpdatesResponse = getUpdatesResponse(serverInfo, settings.wait)
                    continue
                }

                when (lastUpdatesResponse["failed"]?.intOrNullSafe) {
                    1 -> {
                        val newTs = lastUpdatesResponse["ts"]?.contentOrNullSafe
                        if (newTs == null) failNum++

                        lastUpdatesResponse = getUpdatesResponse(
                            serverInfo = serverInfo.copy(ts = newTs ?: serverInfo.ts),
                            wait = settings.wait
                        )
                    }
                    2, 3 -> {
                        serverInfo = getInitialServerInfo().response
                            ?: throw VkResponseException("BotsLongPollApi failed retrieving server info after error: bad VK response (server info #3)")
                        lastUpdatesResponse = getUpdatesResponse(serverInfo, settings.wait)
                    }
                    else -> {
                        val newTs = lastUpdatesResponse["ts"]?.contentOrNullSafe

                        if (newTs == null) {
                            failNum++
                        } else {
                            val updates = lastUpdatesResponse["updates"]?.jsonArrayOrNullSafe

                            if (updates == null) {
                                failNum++
                            } else {
                                updates.forEach { item ->
                                    item.jsonObjectOrNullSafe?.also {
                                        launch {
                                            handleUpdateEvent(it)
                                        }
                                    } ?: failNum++
                                }
                            }

                            lastUpdatesResponse = getUpdatesResponse(serverInfo.copy(ts = newTs), settings.wait)
                        }
                    }
                }
            }
        }
    }

    /**
     * Stop the long polling loop
     */
    public fun stopPolling() {
        try {
            job.cancel()
        } catch (e: Exception) {
        }
    }

    /**
     * Clear listeners for all the events
     */
    public fun clearListeners(): Unit =
        updatesHandler.clearListeners()

    /**
     * Register listener for [type] of events
     *
     * @param type Type key of events
     * @param listener Typed listener
     */
    public fun <T : Any> registerListener(type: String, listener: EventCallback<T>) {
        if (job.isCompleted || job.isCancelled) throw VkException("BotsLongPollApi job is not active when registering a listener")

        updatesHandler.registerListener(type, listener)
    }

    /**
     * Remove [listener]
     * @return true if listener was removed
     */
    public fun unregisterListener(listener: EventCallback<*>): Boolean =
        updatesHandler.unregisterListener(listener)

    /**
     * @return true if [eventJsonObject] is handled successfully
     */
    @Suppress("UNCHECKED_CAST")
    private fun handleUpdateEvent(eventJsonObject: JsonObject): Boolean {
        val eventType = eventJsonObject["type"]?.contentOrNullSafe ?: return false
        val groupId = eventJsonObject["group_id"]?.intOrNullSafe?.also {
            if (it != clientId) return true
        } ?: return false

        val eventObject = eventJsonObject["object"]?.jsonObjectOrNullSafe ?: return false

        return updatesHandler.nextEvent(eventType, eventObject, groupId)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun getInitialServerInfo(): VkResponse<VkLongPollServerResponse> =
        api.call(
            "groups.getLongPollServer",
            paramsOf("group_id" to clientId)
        ).execute().let {
            json.decodeFromString(VkResponseTypedSerializer(VkLongPollServerResponse.serializer()), it)
        }

    private suspend fun getUpdatesResponse(serverInfo: VkLongPollServerResponse, wait: Int): JsonObject? {
        val response = api.getLongPollUpdates(serverInfo, wait)

        return if (response.isSuccessful() && response.body != null) {
            json.parseToJsonElement(response.bodyString()).jsonObjectOrNullSafe
        } else {
            null
        }
    }

    /**
     * Long polling settings
     *
     * @property wait Max wait time if there are no events. Parameter for the request, used by the VK server
     * @property maxFails Max number of errors while handling events before the exception will be thrown. Use [IGNORE_FAILS] to ignore all errors
     */
    public data class Settings(
        val wait: Int = 25,
        val maxFails: Int = IGNORE_FAILS
    ) {
        /**
         * @property IGNORE_FAILS Constant value for ignoring of all errors
         */
        public companion object {
            public const val IGNORE_FAILS: Int = -1
        }
    }
}