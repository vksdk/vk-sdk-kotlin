package com.petersamokhin.vksdk.core.http

import com.petersamokhin.vksdk.core.model.objects.UploadableContent

/**
 * Abstract HTTP client
 */
public interface HttpClient {

    /**
     * Apply client configuration
     *
     * @param config Configuration, such as read and connect timeout, etc
     */
    public fun applyConfig(config: HttpClientConfig)

    /**
     * Make GET request
     *
     * @param url Full request url: host, query, etc.
     */
    public suspend fun get(url: String): Response

    /**
     * Make POST request
     *
     * @param url Full request url: host, query, etc.
     * @param body Request body
     * @param bodyContentType Request body content type
     */
    public suspend fun post(url: String, body: ByteArray, bodyContentType: ContentType): Response

    /**
     * Upload file(s) to this URL
     *
     * @param uploadUrl URL where to upload files
     * @param items List of uploadable items (byte arrays or files)
     */
    public suspend fun postMultipart(uploadUrl: String, items: List<UploadableContent>): Response
}