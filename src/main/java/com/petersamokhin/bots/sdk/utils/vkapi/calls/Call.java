package com.petersamokhin.bots.sdk.utils.vkapi.calls;

import org.json.JSONObject;

/**
 * Abstract class for backward compatibility
 */
public abstract class Call {

    protected String methodName;
    protected JSONObject params;

    public String getMethodName() {
        return methodName;
    }

    public JSONObject getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "Call{" +
                "methodName='" + methodName + '\'' +
                ", params=" + params.toString() +
                '}';
    }
}
