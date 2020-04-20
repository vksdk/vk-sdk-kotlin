package com.petersamokhin.vksdk.http

import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.http.ContentType
import com.petersamokhin.vksdk.core.http.HttpClient
import com.petersamokhin.vksdk.core.http.HttpClientConfig
import com.petersamokhin.vksdk.core.http.Response
import com.petersamokhin.vksdk.core.model.objects.UploadableContent
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * HTTP client based on OkHttp
 *
 * @param config Client configuration
 */
class VkOkHttpClient @JvmOverloads constructor(config: HttpClientConfig = HttpClientConfig()) : HttpClient {
    private var okHttpClient: OkHttpClient? = null

    init {
        applyConfig(config)
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
                val request: Request = chain.request()
                val newRequest: Request

                newRequest = request.newBuilder()
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
     * @param callback Callback to get the result
     */
    override fun get(url: String, callback: Callback<Response>) {
        execute(buildGetRequest(url), callback)
    }

    /**
     * Make GET request
     *
     * @param url Full request url: host, query, etc.
     * @return Response
     */
    override fun getSync(url: String): Response? {
        return executeSync(buildGetRequest(url))
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
        execute(buildPostRequest(url, body, bodyContentType), callback)
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
        return executeSync(buildPostRequest(url, body, bodyContentType))
    }

    /**
     * Upload file(s) to this URL
     *
     * @param uploadUrl URL where to upload files
     * @param items List of uploadable items (byte arrays or files)
     * @param callback Callback to get the result
     */
    override fun postMultipart(uploadUrl: String, items: List<UploadableContent>, callback: Callback<Response>) {
        return execute(buildMultipartRequest(uploadUrl, items), callback)
    }

    /**
     * Upload file(s) to this URL
     *
     * @param uploadUrl URL where to upload files
     * @param items List of uploadable items (byte arrays or files)
     * @return Response
     */
    override fun postMultipartSync(uploadUrl: String, items: List<UploadableContent>): Response? {
        return executeSync(buildMultipartRequest(uploadUrl, items))
    }

    private fun executeSync(request: Request): Response? {
        return (okHttpClient ?: throw IllegalStateException("Apply config first")).let { okHttpClient ->
            okHttpClient.newCall(request).execute().let {
                Response(it.code, it.body?.bytes())
            }
        }
    }

    private fun execute(request: Request, callback: Callback<Response>) {
        (okHttpClient ?: throw IllegalStateException("Apply config first")).also { okHttpClient ->
            okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    callback.onResult(Response(response.code, response.body?.bytes()))
                }
            })
        }
    }

    private fun buildGetRequest(url: String) = Request.Builder().get().url(url).build()

    private fun buildPostRequest(url: String, body: ByteArray, bodyContentType: ContentType): Request {
        return Request.Builder()
            .url(url)
            .post(body.toRequestBody(contentType = bodyContentType.stringValue.toMediaTypeOrNull()))
            .build()
    }

    private fun buildMultipartRequest(uploadUrl: String, items: List<UploadableContent>): Request {
        val formDataBody = MultipartBody.Builder().apply {
            items.forEach { item ->
                when (item) {
                    is UploadableContent.Bytes -> {
                        addFormDataPart(
                            item.fieldName,
                            item.fileName,
                            item.bytes.toRequestBody(item.mediaType.toMediaTypeOrNull())
                        )
                    }
                    is UploadableContent.File -> {
                        addFormDataPart(
                            item.fieldName,
                            item.fileName,
                            item.file.readContent()?.toRequestBody(item.mediaType.toMediaTypeOrNull())
                                ?: throw IllegalStateException("Can't read file from disk")
                        )
                    }
                    is UploadableContent.Url -> {
                        addFormDataPart(
                            item.fieldName,
                            item.fileName,
                            item.url.let { URL(it).readBytes() }.toRequestBody(item.mediaType.toMediaTypeOrNull())
                        )
                    }
                }
            }
            setType("multipart/form-data".toMediaType())
        }.build()

        return Request.Builder()
            .url(uploadUrl)
            .post(formDataBody)
            .build()
    }
}