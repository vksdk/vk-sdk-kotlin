package com.petersamokhin.vksdk.internal.co.touchlab.stately.collections

import com.petersamokhin.vksdk.internal.co.touchlab.stately.isolate.StateHolder
import com.petersamokhin.vksdk.internal.co.touchlab.stately.isolate.createState

open class IsoMutableSet<T> internal constructor(stateHolder: StateHolder<MutableSet<T>>) :
    IsoMutableCollection<T>(stateHolder), MutableSet<T> {
    constructor(producer: () -> MutableSet<T> = { mutableSetOf() }) : this(createState(producer))
}