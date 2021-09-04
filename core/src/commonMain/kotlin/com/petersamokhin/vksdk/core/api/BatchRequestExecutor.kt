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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmOverloads

/**
 * https://vk.com/dev/execute
 */
@OptIn(ExperimentalStdlibApi::class)
public class BatchRequestExecutor @JvmOverloads constructor(
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

    private val resultsChannel = MutableSharedFlow<BatchRequestResult>(0, 64)

    init {
        startQueueLoop()
    }

    /**
     * Add [item] to the [queue]
     */
    public fun enqueue(item: BatchRequestItem) {
        if (settings.maxExecuteRequestsPerSecond == VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED) throw VkSdkInitiationException(
            "BatchRequestExecutor"
        )
        if (job.isCompleted || job.isCancelled) throw VkSdkInitiationException("BatchRequestExecutor")

        requireQueue()
            .addLast(item)
    }

    /**
     * Add [items] to the [queue]
     */
    public fun enqueue(items: Collection<BatchRequestItem>) {
        if (settings.maxExecuteRequestsPerSecond == VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED)
            throw VkSdkInitiationException("BatchRequestExecutor")
        if (job.isCompleted || job.isCancelled)
            throw VkSdkInitiationException("BatchRequestExecutor")

        requireQueue()
            .addAll(items)
    }

    /**
     * Stop the loop
     */
    public fun stop() {
        queue?.dispose()
        queue = null
        job.cancel()
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    public fun observeResults(): Flow<BatchRequestResult> =
        resultsChannel.asSharedFlow()

    /**
     * Loop started during initialization
     */
    private fun startQueueLoop() {
        if (settings.maxExecuteRequestsPerSecond != VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED) {
            queue = IsoArrayDeque()

            launch {
                while (isActive) {
                    executeQueueItems()

                    delay(VkApi.API_CALL_INTERVAL / settings.maxExecuteRequestsPerSecond)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun executeQueueItems(): Job =
        launch {
            if (queue?.isEmpty() == false) {
                val currentItems: List<BatchRequestItem> = pollItemsFromQueue()

                val body = paramsOf("code" to currentItems.buildExecuteCode())
                    .applyRequired()
                    .buildQuery()
                    .encodeToByteArray()

                val response = settings.httpClient.post(
                    "${VkApi.BASE_URL}/execute",
                    body,
                    ContentType.FormUrlEncoded
                )

                val responsesArray: JsonArray? by lazy {
                    response.bodyString().parseResponseArray()
                }

                if (response.body != null && response.isSuccessful() && responsesArray != null) {
                    responsesArray?.forEachIndexed { index, element ->
                        val (request, callback) = currentItems[index]

                        callback.onResult(element)
                        resultsChannel.emit(BatchRequestResult(request, element))
                    }
                } else {
                    currentItems.notifyErrors()
                }
            }
        }

    /**
     * Polls [MAX_QUEUE_ITEMS_COUNT] items from the queue to handle them
     */
    private fun pollItemsFromQueue(): List<BatchRequestItem> =
        requireQueue()
            .access {
                val queue = it as ArrayDeque<BatchRequestItem>

                mutableListOf<BatchRequestItem>().also { list ->
                    for (i in 0 until MAX_QUEUE_ITEMS_COUNT) {
                        list.add(queue.removeFirstOrNull() ?: break)
                    }
                }
            }

    /**
     * Converts the requests to VK API execute method code:
     * https://vk.com/dev/execute
     */
    private fun List<BatchRequestItem>.buildExecuteCode(): String =
        StringBuilder("return [").also { sb ->
            forEach { item: BatchRequestItem ->
                sb.append(item.request.buildExecuteCode()).append(',')
            }
            sb.append("];")
        }.toString()

    private fun List<BatchRequestItem>.notifyErrors() =
        forEach { (_, callback) ->
            callback.onError(VkResponseException("Call to execute method is not successful"))
        }

    private fun requireQueue(): IsoArrayDeque<BatchRequestItem> =
        queue ?: throw VkSdkInitiationException("BatchRequestExecutor")

    private fun VkRequest.buildExecuteCode(): String =
        "API.${method}(${params.buildJsonString()})"

    private fun Parameters.applyRequired(): Parameters =
        apply {
            put(VkApi.ParametersKeys.ACCESS_TOKEN, token)
            put(VkApi.ParametersKeys.VERSION, settings.apiVersion)
            putAll(settings.defaultParams)
        }

    private fun String.parseResponseArray(): JsonArray? =
        json
            .parseToJsonElement(this).jsonObjectOrNullSafe
            ?.get("response")?.jsonArrayOrNullSafe

    public companion object {
        private const val MAX_QUEUE_ITEMS_COUNT = 25
    }
}

