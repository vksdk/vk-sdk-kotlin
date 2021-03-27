package com.petersamokhin.vksdk.core.api.botslongpoll

import co.touchlab.stately.collections.IsoMutableMap
import com.petersamokhin.vksdk.core.callback.EventCallback
import com.petersamokhin.vksdk.core.model.event.MessageNew
import com.petersamokhin.vksdk.core.model.event.RawEvent
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
) : CoroutineScope {
    private val job = SupervisorJob(parentJob)

    override val coroutineContext: CoroutineContext
        get() = backgroundDispatcher + job

    private val listenersMap = IsoMutableMap<String, MutableCollection<EventCallback<*>>>()
    private val jobsMap = IsoMutableMap<String, MutableCollection<Job>>()

    /**
     * Handle next incoming long poll event
     *
     * @param type Type of the event
     * @param eventObject `object` from the event JSON-object
     */
    fun nextEvent(type: String, eventObject: JsonObject, clientId: Int): Boolean {
        listenersMap[RawEvent.TYPE]?.also { listenersList ->
            jobsMap.access { map ->
                map[RawEvent.TYPE] = (map[RawEvent.TYPE] ?: mutableListOf()).also {
                    it += listenersList.map { eventCallback ->
                        launch {
                            @Suppress("UNCHECKED_CAST")
                            (eventCallback as EventCallback<RawEvent>).onEvent(RawEvent(type, eventObject, clientId))
                        }
                    }
                }
            }
        }

        when (type) {
            MessageNew.TYPE -> {
                val parsedEvent: MessageNew = try {
                    json.decodeFromJsonElement(eventObject)
                } catch (t: Throwable) {
                    return false
                }

                listenersMap[type]?.also { listenersList ->
                    jobsMap.access { map ->
                        map[type] = (map[type] ?: mutableListOf()).also {
                            it += listenersList.map { eventCallback ->
                                launch {
                                    @Suppress("UNCHECKED_CAST")
                                    (eventCallback as EventCallback<MessageNew>).onEvent(parsedEvent)
                                }
                            }
                        }
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
     * @return true if listener was registered
     */
    fun <T : Any> registerListener(type: String, listener: EventCallback<T>): Boolean =
        listenersMap.access { map ->
            val result: Boolean

            map[type] = (map[type] ?: mutableListOf()).also {
                result = it.add(listener)
            }

            result
        }

    /**
     * Remove [listener]
     * @return true if listener was removed
     */
    fun unregisterListener(listener: EventCallback<*>): Boolean {
        return listenersMap.access { map ->
            map
                .map { (_, value) -> value.remove(listener) }
                .any { it }
        }
    }

    /**
     * Clear listeners for all the events
     */
    fun clearListeners() {
        listenersMap.clear()
        jobsMap.access { map ->
            map.forEach { (_, list) ->
                list.forEach(Job::cancel)
            }
        }
    }
}