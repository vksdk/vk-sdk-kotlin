package com.petersamokhin.bots.sdk.keyboard;

/**
 * Enum that represent available color for vk buttons
 *
 * @author Igor Mikhailov
 */

public enum ButtonColor {
    PRIMARY("primary"), DEFAULT("default"), NEGATIVE("negative"), POSITIVE("positive");

    private String json;

    ButtonColor(String json) {
        this.json = json;
    }

    public String toJSON() {
        return json;
    }
}