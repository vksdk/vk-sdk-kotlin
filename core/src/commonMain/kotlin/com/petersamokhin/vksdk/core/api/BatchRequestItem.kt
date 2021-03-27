package com.petersamokhin.vksdk.core.api

import com.petersamokhin.vksdk.core.callback.Callback
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmOverloads

/**
 * Wrapper for the request and callback
 *
 * @property request Request info
 * @property callback Callback to get the result
 */
public data class BatchRequestItem @JvmOverloads constructor(
    public val request: VkRequest,
    public val callback: Callback<JsonElement> = Callback.empty()
)