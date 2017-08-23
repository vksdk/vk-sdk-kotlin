package com.petersamokhin.bots.sdk.utils.vkapi;

import com.petersamokhin.bots.sdk.callbacks.callbackapi.ExecuteCallback;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Deserialized class of call to vk api using execute method
 */
public class Call {

    private String methodName;
    private JSONObject params;
    private ExecuteCallback callback;

    public Call(String methodName, JSONObject params, ExecuteCallback callback) {
        this.methodName = methodName;
        this.params = params;
        this.callback = callback;
    }

    public String getMethodName() {
        return methodName;
    }

    public JSONObject getParams() {
        return params;
    }

    public ExecuteCallback getCallback() {
        return callback;
    }

    @Override
    public String toString() {
        return "Call{" +
                "methodName='" + methodName + '\'' +
                ", params=" + params.toString() +
                ", callback=" + "some callback" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Call)) return false;
        Call call = (Call) o;
        return Objects.equals(getMethodName(), call.getMethodName()) &&
                Objects.equals(getParams().toMap(), call.getParams().toMap()) &&
                Objects.equals(getCallback(), call.getCallback());
    }
}