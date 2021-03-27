package com.petersamokhin.vksdk.core.api

import kotlinx.serialization.json.JsonElement

/**
 * Wrapper for the request and it's result
 *
 * @property request Request info
 * @property response Json response
 */
public data class BatchRequestResult(
    public val request: VkRequest,
    public val response: JsonElement
)