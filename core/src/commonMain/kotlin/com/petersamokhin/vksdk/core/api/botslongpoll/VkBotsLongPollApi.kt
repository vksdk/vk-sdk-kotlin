package com.petersamokhin.vksdk.core.api.botslongpoll

import com.petersamokhin.vksdk.core.api.VkApi
import com.petersamokhin.vksdk.core.callback.EventCallback
import com.petersamokhin.vksdk.core.error.VkException
import com.petersamokhin.vksdk.core.error.VkResponseException
import com.petersamokhin.vksdk.core.error.VkSdkInitiationException
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkLongPollServerResponse
import com.petersamokhin.vksdk.core.model.VkResponse
import com.petersamokhin.vksdk.core.model.VkResponseTypedSerializer
import com.petersamokhin.vksdk.core.utils.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import kotlin.coroutines.CoroutineContext

/**
 * Bots LongPoll API
 *
 * @property clientId LongPoll client ID, usually a community ID
 * @property api API wrapper to make network calls
 */
class VkBotsLongPollApi(
    private val clientId: Int,
    private val api: VkApi,
    private val backgroundDispatcher: CoroutineDispatcher
) : CoroutineScope {
    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("BotsLongPollApi::exceptionHandler::error = $throwable")
    }

    /**
     * Coroutine context for jobs
     */
    override val coroutineContext: CoroutineContext
        get() = backgroundDispatcher + job + exceptionHandler

    private val json = defaultJson()

    private val updatesHandler = VkLongPollEventsHandler(json, job, backgroundDispatcher)

    /**
     * Start long polling with settings
     *
     * @param settings Long polling settings, such as wait time for server
     * @return Associated coroutine Job
     */
    fun startPolling(settings: Settings): Job {
        if (job.isCompleted || job.isCancelled) throw VkSdkInitiationException("BotsLongPollApi")

        return launch {
            var serverInfo = getInitialServerInfo().response
                ?: throw VkResponseException("BotsLongPollApi initiation error: bad VK response")
            var lastUpdatesResponse: JsonObject? = getUpdatesResponse(serverInfo, settings.wait)
                ?: throw VkResponseException("BotsLongPollApi initiation error: bad VK response")
            var failNum = 0

            while (job.isActive) {
                if (settings.maxFails != Settings.IGNORE_FAILS && failNum >= settings.maxFails)
                    throw VkResponseException("BotsLongPollApi keeps failing (error #$failNum), last response: $lastUpdatesResponse")

                if (lastUpdatesResponse == null) {
                    failNum++
                    serverInfo = getInitialServerInfo().response
                        ?: throw VkResponseException("BotsLongPollApi failed retrieving server info after error: bad VK response")
                    lastUpdatesResponse = getUpdatesResponse(serverInfo, settings.wait)
                    continue
                }

                when (lastUpdatesResponse["failed"]?.intOrNullSafe) {
                    1 -> {
                        val newTs = lastUpdatesResponse["ts"]?.contentOrNullSafe
                        if (newTs == null) failNum++

                        lastUpdatesResponse = getUpdatesResponse(serverInfo.copy(ts = newTs ?: serverInfo.ts), settings.wait)
                    }
                    2, 3 -> {
                        serverInfo = getInitialServerInfo().response
                            ?: throw VkResponseException("BotsLongPollApi failed retrieving server info after error: bad VK response")
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
    fun stopPolling() {
        try {
            job.cancel()
        } catch (e: Exception) {}
    }

    /**
     * Clear listeners for all the events
     */
    fun clearListeners() = updatesHandler.clearListeners()

    /**
     * Register listener for [type] of events
     *
     * @param type Type key of events
     * @param listener Typed listener
     */
    fun <T: Any> registerListener(type: String, listener: EventCallback<T>) {
        if (job.isCompleted || job.isCancelled) throw VkException("BotsLongPollApi job is not active")

        updatesHandler.registerListener(type, listener)
    }

    /**
     * @return true if [eventJsonObject] is handled successfully
     */
    @OptIn(ImplicitReflectionSerializer::class)
    @Suppress("UNCHECKED_CAST")
    private fun handleUpdateEvent(eventJsonObject: JsonObject): Boolean {
        val eventType = eventJsonObject["type"]?.contentOrNullSafe ?: return false
        val groupId = eventJsonObject["group_id"]?.intOrNullSafe?.also {
            if (it != clientId) return true
        } ?: return false

        val eventObject = eventJsonObject["object"]?.jsonObjectOrNullSafe ?: return false

        return updatesHandler.nextEvent(eventType, eventObject, groupId)
    }

    @OptIn(ImplicitReflectionSerializer::class)
    private fun getInitialServerInfo(): VkResponse<VkLongPollServerResponse> {
        return api.call(
            "groups.getLongPollServer",
            paramsOf("group_id" to clientId)
        ).execute().let {
            json.parse(VkResponseTypedSerializer(VkLongPollServerResponse::class.serializer()), it)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getUpdatesResponse(serverInfo: VkLongPollServerResponse, wait: Int): JsonObject? {
        val response = api.getLongPollUpdates(serverInfo, wait)

        return if (response?.isSuccessful() == true && response.body != null) {
            json.parseJson(response.body.decodeToString()).jsonObjectOrNullSafe
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
    data class Settings(
        val wait: Int = 25,
        val maxFails: Int = IGNORE_FAILS
    ) {
        /**
         * @property IGNORE_FAILS Constant value for ignoring of all errors
         */
        companion object {
            const val IGNORE_FAILS = -1
        }
    }
}