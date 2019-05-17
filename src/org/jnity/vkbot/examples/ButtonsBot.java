package org.jnity.vkbot.examples;

import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Message;
import org.jnity.vkbot.keyboard.Button;
import org.jnity.vkbot.keyboard.ButtonColor;
import org.jnity.vkbot.keyboard.Keyboard;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Voice Bot
 * - any simple text message to voice message
 */
public class ButtonsBot {

    private static boolean isPrime(int n) {
        for (int i = 2; i < n; i++) {
            if (n % i == 0)
                return false;
        }
        return true;
    }

    private static boolean isSquare(int n) {
        int sqrt = (int) Math.sqrt(n);
        if (sqrt * sqrt == n) {
            return true;
        }
        return false;
    }

    private static boolean isPerfect(int n) {
        int sum = 0;
        for (int i = 1; i <= n/2; i++) {
            if (n % i == 0) {
                sum = sum + i;
            }
        }
        return sum == n;
    }

    public static void main(String[] args) throws IOException {

        List<Button> buttons = new ArrayList<>();

        for (int i = 0; i < 40; i++) {
            JSONObject payload = new JSONObject();
            payload.put("number", 100 + i);
            ButtonColor color = ButtonColor.DEFAULT;
            if (isPrime(i))
                color = ButtonColor.NEGATIVE;
            if (isSquare(i))
                color = ButtonColor.POSITIVE;
            if (isPerfect(i))
                color = ButtonColor.PRIMARY;
            Button button = new Button("" + i, color, payload);
            buttons.add(button);
        }
        Keyboard keys = new Keyboard().addButtons(buttons);
        List<String> tokens = Files.readAllLines(new File("../vk_keys.txt").toPath());
        String fullToken = tokens.get(0);
        int groupId = Integer.parseInt(tokens.get(1));
        Group group = new Group(groupId, fullToken);

        group.onSimpleTextMessage(message -> {
            new Message()
                    .from(group)
                    .to(message.authorId())
                    .text(message.getText())
                    .keyboard(keys)
                    // .clearKeyboard()
                    .send();
        });
        // Send error for other messages
        group.onMessage(message -> {
            new Message()
                    .from(group)
                    .to(message.authorId())
                    .text("Sorry, please send me the message that contains only text.")
                    .send();
        });
        //*/
    }
}
