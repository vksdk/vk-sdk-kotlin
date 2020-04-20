package com.petersamokhin.vksdk.http

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.random.Random
import kotlin.test.Test

class JsonTest {
    private val json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true))

    @OptIn(ImplicitReflectionSerializer::class)
    @Test
    fun test() {
        for (i in 0 until 100) {
            println(Random.nextLong(0, Long.MAX_VALUE))
        }
    }
}