package keyboardbot;

/**
 * Class for show how to create keyboard for VK-Bot
 *
 * @author Igor Mikhailov
 */

import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.keyboard.Button;
import com.petersamokhin.bots.sdk.keyboard.Keyboard;
import com.petersamokhin.bots.sdk.objects.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ButtonsBot {

    public static void main(String[] args) throws IOException {

        Keyboard keys = Keyboard.of(new Button("sample", ButtonColor.DEFAULT), new Button("text", ButtonColor.NEGATIVE));
        keys.addButtons(new ArrayList<>());
        keys.addButtons("A", "B", "C", "D", "A1");

        List<String> tokens = Files.readAllLines(new File("../vk_keys.txt").toPath());
        String fullToken = tokens.get(0);
        int groupId = Integer.parseInt(tokens.get(1));
        Group group = new Group(groupId, fullToken);

        group.onSimpleTextMessage(message -> {
            new Message()
                    .from(group)
                    .to(message.authorId())
                    .text("You press " + message.getText())
                    .keyboard(keys)
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
    }
}
