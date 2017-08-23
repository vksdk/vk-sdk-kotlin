package com.petersamokhin.bots.sdk.longpoll;

import org.json.JSONArray;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Queue of updates
 */
class Queue {

    /**
     * List of updates that we need to handle
     */
    volatile CopyOnWriteArrayList<JSONArray> updates = new CopyOnWriteArrayList<>();

    /**
     * We add all of updates from longpoll server
     * to queue
     *
     * @param elements Array of updates
     */
    void putAll(JSONArray elements) {
        elements.forEach(item -> updates.add((JSONArray) item));
    }

    /**
     * Analog method of 'shift()' method from javascript
     *
     * @return First element of list, and then remove it
     */
    JSONArray shift() {
        if (this.updates.size() > 0) {
            JSONArray answer = this.updates.get(0);
            this.updates.remove(0);
            return answer;
        }
        return new JSONArray();
    }
}
