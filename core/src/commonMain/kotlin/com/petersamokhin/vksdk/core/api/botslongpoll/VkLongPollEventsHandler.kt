package com.petersamokhin.vksdk.core.api.botslongpoll

import com.petersamokhin.vksdk.core.callback.EventCallback
import com.petersamokhin.vksdk.core.model.event.MessageNew
import com.petersamokhin.vksdk.core.model.event.RawEvent
import co.touchlab.stately.collections.IsoMutableMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.coroutines.CoroutineContext

/**
 * Handler of all the long poll events
 */
internal class VkLongPollEventsHandler(
    private val json: Json,
    parentJob: Job,
    private val backgroundDispatcher: CoroutineDispatcher
): CoroutineScope {
    private val job = SupervisorJob(parentJob)

    override val coroutineContext: CoroutineContext
        get() = backgroundDispatcher + job

    private val listenersMap: MutableMap<String, MutableCollection<EventCallback<*>>> = IsoMutableMap()

    /**
     * Handle next incoming long poll event
     *
     * @param type Type of the event
     * @param eventObject `object` from the event JSON-object
     */
    fun nextEvent(type: String, eventObject: JsonObject, clientId: Int): Boolean {
        listenersMap[RawEvent.TYPE]?.forEach {
            launch {
                @Suppress("UNCHECKED_CAST")
                (it as EventCallback<RawEvent>).onEvent(RawEvent(type, eventObject, clientId))
            }
        }

        when (type) {
            MessageNew.TYPE -> {
                val parsedEvent: MessageNew = json.decodeFromJsonElement(eventObject)

                listenersMap[type]?.forEach {
                    launch {
                        @Suppress("UNCHECKED_CAST")
                        (it as EventCallback<MessageNew>).onEvent(parsedEvent)
                    }
                }
            }
        }

        return true
    }

    /**
     * Register listener for [type] of events
     *
     * @param type Type key of events
     * @param listener Typed listener
     */
    fun <T: Any> registerListener(type: String, listener: EventCallback<T>) {
        listenersMap[type] = (listenersMap[type] ?: mutableListOf()).also {
            it.add(listener)
        }
    }

    /**
     * Clear listeners for all the events
     */
    fun clearListeners() = listenersMap.clear()
}