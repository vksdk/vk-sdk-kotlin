package com.petersamokhin.vksdk.core.utils

/**
 * `true` -> `1`, `false` -> `0`
 */
internal inline val Boolean.intValue
    get() = if (this) 1 else 0