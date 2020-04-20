package com.petersamokhin.vksdk.internal.co.touchlab.stately.collections

import com.petersamokhin.vksdk.internal.co.touchlab.stately.isolate.IsolateState
import com.petersamokhin.vksdk.internal.co.touchlab.stately.isolate.StateHolder

class IsoMutableIterator<T> internal constructor(stateHolder: StateHolder<MutableIterator<T>>) :
    IsolateState<MutableIterator<T>>(stateHolder), MutableIterator<T> {
    override fun hasNext(): Boolean = access { it.hasNext() }
    override fun next(): T = access { it.next() }
    override fun remove() = access { it.remove() }
}