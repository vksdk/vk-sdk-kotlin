package com.petersamokhin.vksdk.core.api

import com.petersamokhin.vksdk.core.callback.Callback
import kotlinx.serialization.json.JsonElement

/**
 * Wrapper for the request and callback
 *
 * @property request Request info
 * @property callback Callback to get the result
 */
class BatchRequestItem(
    val request: VkRequest,
    val callback: Callback<JsonElement>
)