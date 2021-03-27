package com.petersamokhin.vksdk.core.http

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.jvm.JvmStatic

/**
 * Common class with parameters.
 * Used because it is not possible to have default methods in expect classes.
 */
public class Parameters() {
    /**
     * Wrapped maps with parameters
     */
    private val map = mutableMapOf<String, MutableCollection<Any?>>()

    /**
     * Create parameters from map
     *
     * @param from Original map
     */
    public constructor(from: Map<String, Any?>) : this() {
        map.putAll(mutableMapOf<String, MutableCollection<Any?>>().also {
            from.forEach { (k, v) ->
                when (v) {
                    is Collection<*> -> it[k] = v.toMutableList()
                    else -> {
                        val list: MutableCollection<Any?> = mutableListOf(v ?: "")
                        it[k] = list
                    }
                }
            }
        })
    }

    /**
     * Build URL query from params
     *
     * `Parameters.of("user_ids" to listOf("1", 2, 3))` -> `user_ids=1,2,3`
     *
     * @return Query string
     */
    public fun buildQuery(): String {
        val sb = StringBuilder()
        val entries = map.entries

        entries.forEachIndexed { i, (k, v) ->
            sb.append(k)

            buildValue(v)
                .also { if (it.isNotEmpty()) sb.append('=').append(it) }

            if (i < entries.size - 1) {
                sb.append('&')
            }
        }

        return sb.toString()
    }

    /**
     * Build params as JSON object, e.g. for using in `execute` code.
     *
     * `Parameters.of("user_ids" to listOf("1", 2, 3))` -> `{"user_ids":"1,2,3"}`
     *
     * @return JSON string
     */
    public fun buildJsonString(): String {
        val mapJsonElement = map.mapValues { (_, v) ->
            JsonPrimitive(buildValue(v))
        }

        return JsonObject(mapJsonElement).toString()
    }

    private fun buildValue(v: Collection<Any?>): String =
        v.map { it?.toString().orEmpty() }
            .filter { it.isNotEmpty() }
            .joinToString(",")

    /**
     * Sets [value] for [key]
     *
     * @param key Key for the map
     * @param value Value to put in values list to map
     */
    public fun put(key: String, value: Any?) {
        map[key] = (map[key] ?: mutableListOf()).also {
            when (value) {
                is Collection<*> -> {
                    if (value.isEmpty()) {
                        it.add(EMPTY_VALUE)
                    } else {
                        it.addAll(value)
                    }
                }
                else -> {
                    it.add(value ?: EMPTY_VALUE)
                }
            }
        }
    }

    /**
     * Add all content from another instance of [Parameters]
     *
     * @param other Another instance
     */
    public fun putAll(other: Parameters) {
        other.map.forEach { (k, v) ->
            map[k] = (map[k] ?: mutableListOf()).also {
                it.addAll(v)
            }
        }
    }

    /**
     * Add all content from the other map
     *
     * @param otherMap Other map
     */
    public fun putAll(otherMap: Map<String, Any?>) {
        otherMap.forEach { (k, v) -> put(k, v) }
    }

    /**
     * Sets [value] for [key]
     *
     * @param key Key for the map
     * @param value Value to put in values list to map
     */
    public operator fun set(key: String, value: Any?): Unit =
        put(key, value)

    /**
     * Get value for [key]
     *
     * @param key Key for the map
     */
    public operator fun get(key: String): MutableCollection<Any?>? =
        map[key]

    /**
     * Build string query
     */
    override fun toString(): String =
        buildQuery()

    /**
     * Returns copy of this params as [Map]
     */
    public fun asMap(): Map<String, MutableCollection<Any?>> =
        LinkedHashMap(map)

    public companion object {
        /**
         * Empty value for the param
         */
        internal const val EMPTY_VALUE = ""

        /**
         * Make a parameters map from strings.
         * Each odd parameter is a key for the map,
         * each even parameter is a value.
         * If total count of params is odd, the value for it is the empty string.
         *
         * @param arr List of parameters' keys and values
         * @return Parameters instance
         */
        @JvmStatic
        public fun of(vararg arr: String): Parameters = Parameters().apply {
            val pairs = arr.toList().chunked(2)

            pairs.forEachIndexed { index, list ->
                if (index == pairs.lastIndex) {
                    if (list.size == 1) {
                        put(list[0], EMPTY_VALUE)
                    } else {
                        put(list[0], list[1])
                    }
                } else {
                    put(list[0], list[1])
                }
            }
        }

        /**
         * Make a parameters map from pairs.
         *
         * @return Parameters instance
         */
        @JvmStatic
        public fun of(vararg pairs: Pair<String, Any?>): Parameters =
            Parameters().apply {
                pairs.forEach {
                    put(it.first, it.second)
                }
            }

        /**
         * Make a parameters map from map.
         *
         * @param map Other map
         * @return Parameters instance
         */
        @JvmStatic
        public fun of(map: Map<String, Any?>): Parameters =
            Parameters(map)
    }
}

/**
 * Make a parameters map from strings.
 * Each odd parameter is a key for the map,
 * each even parameter is a value.
 * If total count of params is odd, the value for it is the empty string.
 *
 * @return Parameters instance
 */
public fun paramsOf(vararg arr: String): Parameters =
    Parameters.of(*arr)

/**
 * Make a parameters map from pairs.
 *
 * @return Parameters instance
 */
public fun paramsOf(vararg pairs: Pair<String, Any?>): Parameters =
    Parameters.of(*pairs)

/**
 * Make a parameters map from map.
 *
 * @return Parameters instance
 */
public fun paramsOf(map: Map<String, Any?>): Parameters =
    Parameters.of(map)