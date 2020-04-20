package com.petersamokhin.vksdk.http

import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.error.VkException
import com.petersamokhin.vksdk.core.http.ContentType
import com.petersamokhin.vksdk.core.http.HttpClient
import com.petersamokhin.vksdk.core.http.HttpClientConfig
import com.petersamokhin.vksdk.core.http.Response
import com.petersamokhin.vksdk.core.model.objects.UploadableContent
import com.petersamokhin.vksdk.core.utils.runBlocking
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.ByteArrayContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Abstract ktor HTTP client.
 *
 * Made abstract because this implementation is common
 * and can be used with the any client.
 * So client initiation method is abstract.
 *
 * @param config Basic HTTP client configurations
 * @property coroutineContext Coroutine context for network calls
 */
abstract class VkKtorHttpClient constructor(
    config: HttpClientConfig,
    override val coroutineContext: CoroutineContext
) : HttpClient, CoroutineScope {
    @Suppress("LeakingThis")
    private var client = io.ktor.client.HttpClient(createEngineWithConfig(config))

    /**
     * Apply client configuration
     *
     * @param config Configuration, such as read and connect timeout, etc
     */
    override fun applyConfig(config: HttpClientConfig) {
        client = io.ktor.client.HttpClient(createEngineWithConfig(config))
    }

    /**
     * Make GET request
     *
     * @param url Full request url: host, query, etc.
     * @return Response
     */
    override fun getSync(url: String): Response? {
        return runBlocking {
            val response = try {
                client.get<HttpResponse> {
                    url(url)
                }
            } catch (e: Exception) {
                throw VkException(cause = e)
            }

            Response(response.status.value, response.readBytes())
        }
    }

    /**
     * Make POST request
     *
     * @param url Full request url: host, query, etc.
     * @param body Request body
     * @param bodyContentType Request body content type
     * @return Response
     */
    override fun postSync(url: String, body: ByteArray, bodyContentType: ContentType): Response? {
        return runBlocking(coroutineContext) {
            val response = try {
                client.post<HttpResponse> {
                    url(url)
                    this.body = ByteArrayContent(body, bodyContentType.toKtorContentType())
                }
            } catch (e: Exception) {
                throw VkException(cause = e)
            }

            Response(response.status.value, response.readBytes())
        }
    }

    /**
     * Make GET request
     *
     * @param url Full request url: host, query, etc.
     * @param callback Callback to get the result
     */
    override fun get(url: String, callback: Callback<Response>) {
        launch {
            try {
                val response = client.get<HttpResponse> {
                    url(url)
                }

                callback.onResult(Response(response.status.value, response.readBytes()))
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Make POST request
     *
     * @param url Full request url: host, query, etc.
     * @param body Request body
     * @param bodyContentType Request body content type
     * @param callback Callback to get the result
     */
    override fun post(url: String, body: ByteArray, bodyContentType: ContentType, callback: Callback<Response>) {
        launch {
            try {
                val response = client.post<HttpResponse> {
                    url(url)
                    this.body = ByteArrayContent(body, bodyContentType.toKtorContentType())
                }

                callback.onResult(Response(response.status.value, response.readBytes()))
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Upload file(s) to this URL
     *
     * @param uploadUrl URL where to upload files
     * @param items List of uploadable items (byte arrays or files)
     * @param callback Callback to get the result
     */
    override fun postMultipart(uploadUrl: String, items: List<UploadableContent>, callback: Callback<Response>) {
        launch {
            try {
                val response = callMultipart(uploadUrl, items)

                callback.onResult(Response(response.status.value, response.readBytes()))
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Upload file(s) to this URL
     *
     * @param uploadUrl URL where to upload files
     * @param items List of uploadable items (byte arrays or files)
     * @return Response
     */
    override fun postMultipartSync(uploadUrl: String, items: List<UploadableContent>): Response? {
        return runBlocking(coroutineContext) {
            val response = callMultipart(uploadUrl, items)

            Response(response.status.value, response.readBytes())
        }
    }

    private suspend fun callMultipart(uploadUrl: String, items: List<UploadableContent>): HttpResponse {
        return client.post {
            url(uploadUrl)
            body = MultiPartFormDataContent(formData {
                items.forEach {
                    val headers = Headers.build {
                        set(HttpHeaders.ContentDisposition, it.contentDisposition())
                    }

                    when (it) {
                        is UploadableContent.Bytes -> {
                            append(it.fieldName, it.bytes, headers)
                        }
                        is UploadableContent.File -> {
                            append(it.fieldName, it.file.readContent() ?: throw VkException("Can't read file contents"), headers)
                        }
                        is UploadableContent.Url -> {
                            append(it.fieldName, getSync(it.url)?.body ?: throw VkException("Can't read URL contents"), headers)
                        }
                    }
                }
            })
        }
    }

    private fun ContentType.toKtorContentType(): io.ktor.http.ContentType {
        return when (this) {
            ContentType.FormUrlEncoded -> io.ktor.http.ContentType.Application.FormUrlEncoded
        }
    }

    /**
     * Instantiate desired client and apply basic configurations
     *
     * @param config Basic configurations
     * @return Desired HTTP client engine, e.g. CIO, etc.
     */
    abstract fun createEngineWithConfig(config: HttpClientConfig): HttpClientEngine
}