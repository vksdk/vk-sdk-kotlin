package com.petersamokhin.bots.sdk.longpoll;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Queue of updates
 */
class Queue {

    volatile List<JSONArray> updates = new ArrayList<>();

    void putAll(JSONArray elements) {
        for (int i = 0; i < elements.length(); i++) {
            updates.add(elements.getJSONArray(i));
        }
    }

    JSONArray shift() {
        JSONArray answer = new JSONArray();
        if (this.updates.size() > 0) {
            answer = this.updates.get(0);
            this.updates.remove(0);
        }
        return answer;
    }
}
