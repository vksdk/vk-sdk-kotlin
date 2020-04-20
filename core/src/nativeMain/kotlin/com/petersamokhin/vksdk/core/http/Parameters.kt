package com.petersamokhin.vksdk.core.http

actual class Parameters : ParametersCommon {
    actual constructor() : super()

    actual constructor(from: Map<String, Any?>) : super(from)

    actual companion object {
        actual fun of(vararg arr: String): Parameters = ParametersCommon.of(*arr)

        actual fun of(vararg pairs: Pair<String, Any?>): Parameters = ParametersCommon.of(*pairs)

        actual fun of(map: Map<String, Any?>): Parameters = ParametersCommon.of(map)
    }
}