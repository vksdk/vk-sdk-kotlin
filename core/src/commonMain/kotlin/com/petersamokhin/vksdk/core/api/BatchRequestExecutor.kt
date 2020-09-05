package com.petersamokhin.vksdk.core.api

import co.touchlab.stately.collections.IsoArrayDeque
import com.petersamokhin.vksdk.core.error.VkResponseException
import com.petersamokhin.vksdk.core.error.VkSdkInitiationException
import com.petersamokhin.vksdk.core.http.ContentType
import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.core.utils.defaultJson
import com.petersamokhin.vksdk.core.utils.jsonArrayOrNullSafe
import com.petersamokhin.vksdk.core.utils.jsonObjectOrNullSafe
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmOverloads

/**
 * https://vk.com/dev/execute
 */
@OptIn(ExperimentalStdlibApi::class)
class BatchRequestExecutor @JvmOverloads constructor(
    private val token: String,
    private val settings: VkSettings,
    private val json: Json = defaultJson()
) : CoroutineScope {
    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("BatchRequestExecutor::exceptionHandler::error = $throwable")
        throwable.printStackTrace()
    }

    /**
     * Coroutine context for the loop
     */
    override val coroutineContext: CoroutineContext
        get() = settings.backgroundDispatcher + job + exceptionHandler

    private var queue: IsoArrayDeque<BatchRequestItem>? = null

    init {
        if (settings.maxExecuteRequestsPerSecond != VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED) {
            queue = IsoArrayDeque()

            launch {
                while (isActive) {
                    launch {
                        if (queue?.isEmpty() == false) {
                            val currentItems = (queue ?: throw VkSdkInitiationException("BatchRequestExecutor"))
                                .access {
                                    val queue = it as ArrayDeque<BatchRequestItem>

                                    mutableListOf<BatchRequestItem>().also { list ->
                                        for (i in 0 until MAX_QUEUE_ITEMS_COUNT) {
                                            list.add(queue.removeFirstOrNull() ?: break)
                                        }
                                    }
                                }

                            fun notifyErrorsAll() {
                                currentItems.forEach {
                                    it.callback.onError(VkResponseException("Call to execute method is not successful"))
                                }
                            }

                            val codeStringBuilder = StringBuilder("return [").also { sb ->
                                currentItems.forEach { item ->
                                    sb.append(item.request.buildExecuteCode()).append(',')
                                }
                                sb.append("];")
                            }

                            val body = paramsOf("code" to codeStringBuilder.toString())
                                .applyRequired()
                                .buildQuery()
                                .encodeToByteArray()

                            val response = settings.httpClient.postSync(
                                "${VkApi.BASE_URL}/execute",
                                body,
                                ContentType.FormUrlEncoded
                            )

                            if (response?.body != null && response.isSuccessful()) {
                                json.parseToJsonElement(response.bodyString()).jsonObjectOrNullSafe?.also { bodyJson ->
                                    val responseJson = bodyJson["response"]?.jsonArrayOrNullSafe

                                    responseJson?.forEachIndexed { index, element ->
                                        currentItems[index].callback.onResult(element)
                                    } ?: notifyErrorsAll()
                                } ?: notifyErrorsAll()
                            } else {
                                notifyErrorsAll()
                            }
                        }
                    }

                    delay(VkApi.API_CALL_INTERVAL / settings.maxExecuteRequestsPerSecond)
                }
            }
        }
    }

    /**
     * Add [item] to the [queue]
     */
    fun enqueue(item: BatchRequestItem) {
        if (settings.maxExecuteRequestsPerSecond == VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED) throw VkSdkInitiationException("BatchRequestExecutor")
        if (job.isCompleted || job.isCancelled) throw VkSdkInitiationException("BatchRequestExecutor")

        (queue ?: throw VkSdkInitiationException("BatchRequestExecutor"))
            .addLast(item)
    }

    /**
     * Add [items] to the [queue]
     */
    fun enqueue(items: Collection<BatchRequestItem>) {
        if (settings.maxExecuteRequestsPerSecond == VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED) throw VkSdkInitiationException("BatchRequestExecutor")
        if (job.isCompleted || job.isCancelled) throw VkSdkInitiationException("BatchRequestExecutor")

        (queue ?: throw VkSdkInitiationException("BatchRequestExecutor"))
            .addAll(items)
    }

    /**
     * Stop the loop
     */
    fun stop() {
        queue?.dispose()
        queue = null
        job.cancel()
    }

    private fun VkRequest.buildExecuteCode(): String {
        return "API.${method}(${params.buildJsonString()})"
    }

    private fun Parameters.applyRequired() = apply {
        put(VkApi.ParametersKeys.ACCESS_TOKEN, token)
        put(VkApi.ParametersKeys.VERSION, settings.apiVersion)
        putAll(settings.defaultParams)
    }

    companion object {
        private const val MAX_QUEUE_ITEMS_COUNT = 25
    }
}

