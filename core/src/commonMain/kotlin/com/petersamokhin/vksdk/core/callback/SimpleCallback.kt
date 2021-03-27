package com.petersamokhin.vksdk.core.callback

import kotlin.jvm.JvmStatic

/**
 * Simple callback, used mostly for interoperability with Java
 */
public fun interface SimpleCallback<in T : Any> {
    public fun accept(value: T)

    public companion object {
        @JvmStatic
        public fun <T : Any> empty(): SimpleCallback<T> =
            SimpleCallback {}
    }
}