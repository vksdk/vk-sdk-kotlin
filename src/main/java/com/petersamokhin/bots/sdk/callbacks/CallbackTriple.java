package com.petersamokhin.bots.sdk.callbacks;

public interface CallbackTriple<T, M, R> extends AbstractCallback {
    void onEvent(T t, M m, R r);
}
