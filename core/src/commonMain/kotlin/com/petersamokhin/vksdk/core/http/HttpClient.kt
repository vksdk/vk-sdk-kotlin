package com.petersamokhin.vksdk.core.http

import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.model.objects.UploadableContent

/**
 * Abstract HTTP client
 */
interface HttpClient {

    /**
     * Apply client configuration
     *
     * @param config Configuration, such as read and connect timeout, etc
     */
    fun applyConfig(config: HttpClientConfig)

    /**
     * Make GET request
     *
     * @param url Full request url: host, query, etc.
     * @param callback Callback to get the result
     */
    fun get(url: String, callback: Callback<Response>)

    /**
     * Make POST request
     *
     * @param url Full request url: host, query, etc.
     * @param body Request body
     * @param bodyContentType Request body content type
     * @param callback Callback to get the result
     */
    fun post(url: String, body: ByteArray, bodyContentType: ContentType, callback: Callback<Response>)

    /**
     * Make GET request
     *
     * @param url Full request url: host, query, etc.
     * @return Response
     */
    fun getSync(url: String): Response?

    /**
     * Make POST request
     *
     * @param url Full request url: host, query, etc.
     * @param body Request body
     * @param bodyContentType Request body content type
     * @return Response
     */
    fun postSync(url: String, body: ByteArray, bodyContentType: ContentType): Response?

    /**
     * Upload file(s) to this URL
     *
     * @param uploadUrl URL where to upload files
     * @param items List of uploadable items (byte arrays or files)
     * @return Response
     */
    fun postMultipartSync(uploadUrl: String, items: List<UploadableContent>): Response?

    /**
     * Upload file(s) to this URL
     *
     * @param uploadUrl URL where to upload files
     * @param items List of uploadable items (byte arrays or files)
     * @param callback Callback to get the result
     */
    fun postMultipart(uploadUrl: String, items: List<UploadableContent>, callback: Callback<Response>)
}