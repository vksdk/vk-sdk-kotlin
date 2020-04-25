package com.petersamokhin.vksdk.core.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonException
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Default json de/serializer for the project
 */
internal fun defaultJson(): Json {
    return Json(JsonConfiguration.Stable.copy(encodeDefaults = false, ignoreUnknownKeys = true))
}

/**
 * Get wrapped value or null without exceptions
 */
val JsonElement.intOrNullSafe
    get() = try {
        intOrNull
    } catch (e: JsonException) {
        null
    }

/**
 * Get wrapped value or null without exceptions
 */
val JsonElement.contentOrNullSafe
    get() = try {
        contentOrNull
    } catch (e: JsonException) {
        null
    }

/**
 * Get wrapped value or null without exceptions
 */
val JsonElement.jsonArrayOrNullSafe
    get() = try {
        jsonArray
    } catch (e: JsonException) {
        null
    }

/**
 * Get wrapped value or null without exceptions
 */
val JsonElement.jsonObjectOrNullSafe
    get() = try {
        jsonObject
    } catch (e: JsonException) {
        null
    }