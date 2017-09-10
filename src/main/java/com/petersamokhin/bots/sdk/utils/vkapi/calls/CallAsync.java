package com.petersamokhin.bots.sdk.utils.vkapi.calls;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Deserialized class of call to vk api using execute method
 */
public class CallAsync extends Call {

    private Callback<Object> callback;

    public CallAsync(String methodName, JSONObject params, Callback<Object> callback) {
        this.methodName = methodName;
        this.params = params;
        this.callback = callback;
    }

    public Callback<Object> getCallback() {
        return callback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallAsync)) return false;
        CallAsync call = (CallAsync) o;
        return Objects.equals(getMethodName(), call.getMethodName()) &&
                Objects.equals(getParams().toMap(), call.getParams().toMap()) &&
                Objects.equals(getCallback(), call.getCallback());
    }
}