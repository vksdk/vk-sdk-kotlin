package com.petersamokhin.bots.sdk.callbacks;

public interface CallbackDouble<T, R> extends AbstractCallback {
    void onEvent(T t, R r);
}
