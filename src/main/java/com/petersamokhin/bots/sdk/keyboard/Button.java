package com.petersamokhin.bots.sdk.keyboard;

import org.json.JSONObject;

/**
 * Implementation of single button for VK-Bot API
 * Payload parameter at this moment is useless
 *
 * @author Igor Mikhailov
 */

public class Button {

    private static final JSONObject EMPTY_PAYLOAD = new JSONObject();
    private final String label;
    private final ButtonColor color;
    private final JSONObject payload;

    public Button(String label, ButtonColor color, JSONObject payload) {
        this.label = label;
        this.color = color;
        this.payload = payload;
    }

    public Button(String label) {
        this(label, ButtonColor.DEFAULT, EMPTY_PAYLOAD);
    }

    public Button(String label, ButtonColor color) {
        this(label, color, EMPTY_PAYLOAD);
    }

    public Button(String label, JSONObject payload) {
        this(label, ButtonColor.DEFAULT, payload);
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        JSONObject action = new JSONObject();
        action.put("type", "text");
        action.put("payload", payload.toString());
        action.put("label", label);
        result.put("action", action);
        result.put("color", color.toJSON());
        return result;
    }

    public String toString() {
        return toJSON().toString();
    }
}