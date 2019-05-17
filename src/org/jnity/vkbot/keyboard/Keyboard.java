package org.jnity.vkbot.keyboard;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Keyboard {

    private static final int MAX_BUTTONS_IN_LINES = 4;
    private static final int MAX_LINES = 10;
    private static final int MAX_BUTTONS = MAX_BUTTONS_IN_LINES * MAX_LINES;
    private boolean one_time = false;
    private List<List<Button>> buttonsLines = new ArrayList<>();


    public Keyboard setOneTime(boolean value) {
        one_time = value;
        return this;
    }

    public Keyboard addButtons(List<Button> buttons) {
        if (buttonsLines.size() * MAX_BUTTONS_IN_LINES + buttons.size() > MAX_BUTTONS) {
            throw new IllegalArgumentException("Too many buttons, max is " + MAX_BUTTONS);
        }
        int lineCounter = 0;
        List<Button> buttonsLine = new ArrayList<>();
        for (Button button : buttons) {
            buttonsLine.add(button);
            lineCounter++;
            if (lineCounter == 4) {
                buttonsLines.add(buttonsLine);
                lineCounter = 0;
                buttonsLine = new ArrayList<>();
            }
        }
        if (lineCounter > 0) {
            buttonsLines.add(buttonsLine);
        }
        return this;
    }

    public Keyboard addButtons(Button... buttons) {
        return addButtons(Arrays.asList(buttons));
    }


    public Keyboard addButtons(String... buttons) {
        return addButtons(Arrays.stream(buttons).map(Button::new).collect(Collectors.toList()));
    }

    public static Keyboard of(String... buttons) {
        Keyboard result = new Keyboard();
        result.addButtons(buttons);
        return result;
    }

    public static Keyboard of(Button... buttons) {
        Keyboard result = new Keyboard();
        result.addButtons(buttons);
        return result;
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        JSONArray buttons = new JSONArray();
        for (List<Button> line : buttonsLines) {
            JSONArray jsonLine = new JSONArray();
            for (Button button : line) {
                jsonLine.put(button.toJSON());
            }
            buttons.put(jsonLine);
        }
        result.put("buttons", buttons);
        result.put("one_time", one_time);
        return result;
    }

    public String toString() {
        return toJSON().toString();
    }
}
