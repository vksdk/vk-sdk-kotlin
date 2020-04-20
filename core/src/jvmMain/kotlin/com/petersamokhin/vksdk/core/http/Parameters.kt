package com.petersamokhin.vksdk.core.http

/**
 * Request parameters, simply the map wrapper with some utility methods.
 * The common class is `expect` to be able to have
 * dedicated Java implementation with more convenient API,
 * e.g. with static and overloaded methods.
 */
actual class Parameters : ParametersCommon {
    /**
     * Create empty parameters instance
     */
    actual constructor(): super()

    /**
     * Create parameters from map
     *
     * @param from Original map
     */
    actual constructor(from: Map<String, Any?>): super(from)

    actual companion object {
        /**
         * Make a parameters map from strings.
         * Each odd parameter is a key for the map,
         * each even parameter is a value.
         * If total count of params is odd, the value for it is the empty string.
         *
         * @return Parameters instance
         */
        @JvmStatic
        actual fun of(vararg arr: String): Parameters = ParametersCommon.of(*arr)

        /**
         * Make a parameters map from pairs.
         *
         * @return Parameters instance
         */
        @JvmStatic
        actual fun of(vararg pairs: Pair<String, Any?>): Parameters = ParametersCommon.of(*pairs)

        /**
         * Make a parameters map from map.
         *
         * @return Parameters instance
         */
        @JvmStatic
        actual fun of(map: Map<String, Any?>): Parameters = ParametersCommon.of(map)
    }
}