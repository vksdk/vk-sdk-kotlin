package com.petersamokhin.vksdk.http

import com.petersamokhin.vksdk.core.http.Parameters
import kotlin.test.Test
import kotlin.test.assertEquals

class ParametersTest {
    @Test
    fun build_of_strings() {
        val params = Parameters.of("key", "value", "and_another", "too", "and_another_one", "yep", "user_id", "1")

        assertEquals("key=value&and_another=too&and_another_one=yep&user_id=1", params.buildQuery())
    }

    @Test
    fun build_instantiated_from_map() {
        val params = Parameters(mapOf("key" to "value", "and_another" to "too", "user_id" to 1))

        assertEquals("key=value&and_another=too&user_id=1", params.buildQuery())
    }

    @Test
    fun build_of_empty_param() {
        val params = Parameters.of("key", "value", "and_another_one")

        assertEquals("key=value&and_another_one", params.buildQuery())
    }

    @Test
    fun build_of_list_param() {
        val params = Parameters.of("key" to "value", "and_another_one" to listOf("too"))

        assertEquals("key=value&and_another_one=too", params.buildQuery())
    }

    @Test
    fun build_json_of_simple_strings() {
        val params = Parameters.of("key", "value", "and_another", "too")

        //language=JSON
        assertEquals("""{"key":"value","and_another":"too"}""", params.buildJsonString())
    }

    @Test
    fun build_json_of_list_param() {
        val params = Parameters.of("key" to "value", "and_another" to listOf("too", "yep"))

        //language=JSON
        assertEquals("""{"key":"value","and_another":"too,yep"}""", params.buildJsonString())
    }
}