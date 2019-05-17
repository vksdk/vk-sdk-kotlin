package org.jnity.vkbot.keyboard;

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