package com.petersamokhin.vksdk.http

import com.petersamokhin.vksdk.core.http.ContentType
import com.petersamokhin.vksdk.core.http.HttpClient
import com.petersamokhin.vksdk.core.http.HttpClientConfig
import com.petersamokhin.vksdk.core.http.Response
import com.petersamokhin.vksdk.core.model.objects.UploadableContent
import com.petersamokhin.vksdk.http.utils.await
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * HTTP client based on OkHttp
 */
@ExperimentalCoroutinesApi
public class VkOkHttpClient : HttpClient {
    private var okHttpClient: OkHttpClient? = null

    /**
     *  @param config Client configuration
     */
    @JvmOverloads
    public constructor(config: HttpClientConfig = HttpClientConfig()) {
        applyConfig(config)
    }

    /**
     * @param overrideClient Provide yours client
     */
    public constructor(overrideClient: OkHttpClient) {
        okHttpClient = overrideClient
    }

    /**
     * Apply client configuration
     *
     * @param config Configuration, such as read and connect timeout, etc
     */
    override fun applyConfig(config: HttpClientConfig) {
        okHttpClient = OkHttpClient.Builder()
            .readTimeout(config.readTimeout.toLong(), TimeUnit.MILLISECONDS)
            .connectTimeout(config.connectTimeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .also {
                        if (config.defaultHeaders.isNotEmpty()) {
                            config.defaultHeaders.forEach { (k, v) ->
                                it.addHeader(k, v)
                            }
                        }
                    }
                    .build()

                chain.proceed(newRequest)
            }
            .build()
    }

    /**
     * Make GET request
     *
     * @param url Full request url: host, query, etc.
     */
    override suspend fun get(url: String): Response =
        execute(buildGetRequest(url)).toResponse()

    /**
     * Make POST request
     *
     * @param url Full request url: host, query, etc.
     * @param body Request body
     * @param bodyContentType Request body content type
     */
    override suspend fun post(url: String, body: ByteArray, bodyContentType: ContentType): Response =
        execute(buildPostRequest(url, body, bodyContentType)).toResponse()

    /**
     * Upload file(s) to this URL
     *
     * @param uploadUrl URL where to upload files
     * @param items List of uploadable items (byte arrays or files)
     */
    override suspend fun postMultipart(uploadUrl: String, items: List<UploadableContent>): Response =
        execute(buildMultipartRequest(uploadUrl, items)).toResponse()

    @ExperimentalCoroutinesApi
    private suspend fun execute(request: Request): okhttp3.Response =
        requireOkHttpClient().newCall(request).await()

    private fun buildGetRequest(url: String): Request =
        Request.Builder()
            .get()
            .url(url)
            .build()

    private fun buildPostRequest(url: String, body: ByteArray, bodyContentType: ContentType): Request =
        Request.Builder()
            .url(url)
            .post(body.toRequestBody(contentType = bodyContentType.stringValue.toMediaTypeOrNull()))
            .build()

    private fun buildMultipartRequest(uploadUrl: String, items: List<UploadableContent>): Request {
        val formDataBody = MultipartBody.Builder().apply {
            setType("multipart/form-data".toMediaType())

            items.forEach { item ->
                when (item) {
                    is UploadableContent.Bytes -> {
                        addFormDataPart(
                            name = item.fieldName,
                            filename = item.fileName,
                            body = item.bytes.toRequestBody(item.mediaType.toMediaTypeOrNull())
                        )
                    }
                    is UploadableContent.File -> {
                        addFormDataPart(
                            name = item.fieldName,
                            filename = item.fileName,
                            body = item.file.readContent()?.toRequestBody(item.mediaType.toMediaTypeOrNull())
                                ?: throw IllegalStateException("Can't read file from disk")
                        )
                    }
                    is UploadableContent.Url -> {
                        addFormDataPart(
                            name = item.fieldName,
                            filename = item.fileName,
                            body = item.url.let { URL(it).readBytes() }.toRequestBody(item.mediaType.toMediaTypeOrNull())
                        )
                    }
                }
            }
        }.build()

        return Request.Builder()
            .url(uploadUrl)
            .post(formDataBody)
            .build()
    }

    private fun requireOkHttpClient(): OkHttpClient =
        okHttpClient ?: throw IllegalStateException("Apply config first")

    private fun okhttp3.Response.toResponse(): Response =
        Response(code, body?.bytes())
}