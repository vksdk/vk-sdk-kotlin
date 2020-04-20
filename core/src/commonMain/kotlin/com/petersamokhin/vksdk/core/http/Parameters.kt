package com.petersamokhin.vksdk.core.http

/**
 * Request parameters, simply the map wrapper with some utility methods.
 * The common class is `expect` to be able to have
 * dedicated Java implementation with more convenient API,
 * e.g. with static and overloaded methods.
 */
expect class Parameters(): ParametersCommon {

    /**
     * Create parameters from map
     *
     * @param from Original map
     */
    constructor(from: Map<String, Any?>)

    companion object {
        /**
         * Make a parameters map from strings.
         * Each odd parameter is a key for the map,
         * each even parameter is a value.
         * If total count of params is odd, the value for it is the empty string.
         *
         * @return Parameters instance
         */
        fun of(vararg arr: String): Parameters

        /**
         * Make a parameters map from pairs.
         *
         * @return Parameters instance
         */
        fun of(vararg pairs: Pair<String, Any?>): Parameters

        /**
         * Make a parameters map from map.
         *
         * @return Parameters instance
         */
        fun of(map: Map<String, Any?>): Parameters
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
fun paramsOf(vararg arr: String) = Parameters.of(*arr)

/**
 * Make a parameters map from pairs.
 *
 * @return Parameters instance
 */
fun paramsOf(vararg pairs: Pair<String, Any?>) = Parameters.of(*pairs)

/**
 * Make a parameters map from map.
 *
 * @return Parameters instance
 */
fun paramsOf(map: Map<String, Any?>) = Parameters.of(map)