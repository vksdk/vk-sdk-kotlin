package com.petersamokhin.bots.sdk.callbacks;

/**
 * Created by PeterSamokhin on 29/09/2017 00:57
 */
public interface CallbackFourth<P, D, R, S> extends AbstractCallback {

    void onEvent(P i, D a, R as, S ti);
}
