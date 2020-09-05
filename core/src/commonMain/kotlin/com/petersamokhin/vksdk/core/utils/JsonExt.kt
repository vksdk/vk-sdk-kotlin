package com.petersamokhin.vksdk.core.utils

import kotlinx.serialization.json.*

/**
 * Default json de/serializer for the project
 */
internal fun defaultJson(): Json {
    return Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }
}

/**
 * Get wrapped value or null without exceptions
 */
internal val JsonElement.intOrNullSafe
    get() = try {
        jsonPrimitive.intOrNull
    } catch (e: IllegalArgumentException) {
        null
    }

/**
 * Get wrapped value or null without exceptions
 */
internal val JsonElement.contentOrNullSafe
    get() = try {
        jsonPrimitive.contentOrNull
    } catch (e: IllegalArgumentException) {
        null
    }

/**
 * Get wrapped value or null without exceptions
 */
internal val JsonElement.jsonArrayOrNullSafe
    get() = try {
        jsonArray
    } catch (e: IllegalArgumentException) {
        null
    }

/**
 * Get wrapped value or null without exceptions
 */
internal val JsonElement.jsonObjectOrNullSafe
    get() = try {
        jsonObject
    } catch (e: IllegalArgumentException) {
        null
    }