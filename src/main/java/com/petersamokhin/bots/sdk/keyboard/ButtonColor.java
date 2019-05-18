package com.petersamokhin.bots.sdk.keyboard;

/**
 * Enum that represent color available for VK buttons
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