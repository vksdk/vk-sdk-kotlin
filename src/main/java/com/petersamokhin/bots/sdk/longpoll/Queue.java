package com.petersamokhin.bots.sdk.longpoll;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Queue of updates
 */
class Queue {

    /**
     * List of updates that we need to handle
     */
    volatile List<JSONArray> updates = new ArrayList<>();

    /**
     * We add all of updates from longpoll server
     * to queue
     *
     * @param elements Array of updates
     */
    void putAll(JSONArray elements) {
        for (int i = 0; i < elements.length(); i++) {
            updates.add(elements.getJSONArray(i));
        }
    }

    /**
     * Analog method of 'shift()' method from javascript
     *
     * @return First element of list, and then remove it
     */
    JSONArray shift() {
        JSONArray answer = new JSONArray();
        if (this.updates.size() > 0) {
            answer = this.updates.get(0);
            this.updates.remove(0);
        }
        return answer;
    }
}
