package com.petersamokhin.examples;

import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Message;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * QRCode Bot Example
 * Usage:
 * - any text will be converted to the QR Code
 * - any image that contains QR Code will be readed and text will be sent to the user
 */
public class Main {

    public static void main(String... args) {


        Group group = new Group(151083290, "access_token");

        group.onSimpleTextMessage(message -> {

            // Creating file if it is not exists
            File QRCodeImage = new File("template_" + message.authorId() + ".png");
            try {
                QRCodeImage.createNewFile();
                Files.setPosixFilePermissions(Paths.get(QRCodeImage.getAbsolutePath()), PosixFilePermissions.fromString("rwxrwxrwx"));
            } catch (IOException ignored) {
                System.out.println("Some error occured: " + ignored.toString() + " with file " + QRCodeImage.getAbsolutePath());
            }

            // Encode the text to the image
            QRCode qr = new QRCode();
            qr.encode(message.getText(), QRCodeImage);

            // Send message
            new Message()
                    .from(group)
                    .to(message.authorId())
                    .photo(QRCodeImage.getAbsolutePath())
                    .send();

            // Delete template file
            try {
                Files.delete(Paths.get(QRCodeImage.getAbsolutePath()));
            } catch (IOException ignored) {
            }
        });

        group.onPhotoMessage(message -> {

            // Creating file if he is not exists
            File QRCodeImage = new File("template_" + message.authorId() + ".png");
            try {
                QRCodeImage.createNewFile();
                Files.setPosixFilePermissions(Paths.get(QRCodeImage.getAbsolutePath()), PosixFilePermissions.fromString("rwxrwxrwx"));
            } catch (IOException ignored) {
                System.out.println("Some error occured: " + ignored.toString() + " with file " + QRCodeImage.getAbsolutePath());
            }

            // Downloading image to file
            try {
                FileUtils.copyURLToFile(new URL(
                        message.getBiggestPhotoUrl(message.getPhotos())
                ), QRCodeImage);
            } catch (IOException ignored) {
                return;
            }

            QRCode qr = new QRCode();

            // Send message
            new Message()
                    .from(group)
                    .to(message.authorId())
                    .text(qr.decode(QRCodeImage))
                    .send();

            // Delete template file
            try {
                Files.delete(Paths.get(QRCodeImage.getAbsolutePath()));
            } catch (IOException ignored) {
            }
        });
    }
}
