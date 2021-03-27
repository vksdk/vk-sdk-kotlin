package com.petersamokhin.vksdk.core.http

/**
 * Abstract HTTP Client config
 *
 * @property connectTimeout Connect timeout in milliseconds
 * @property readTimeout Read timeout in milliseconds
 * @property defaultHeaders Default HTTP headers, such as `User-Agent`
 */
public data class HttpClientConfig(
    val connectTimeout: Int = 30_000,
    val readTimeout: Int = 30_000,
    val defaultHeaders: Map<String, String> = mapOf()
)