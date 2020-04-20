package com.petersamokhin.vksdk.core.utils

internal inline val Boolean.intValue
    get() = if (this) 1 else 0