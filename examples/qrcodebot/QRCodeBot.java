package com.petersamokhin.bots.sdk.examples;

import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Message;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * Bot that can generate QR Code from any text and recognize text from QR Code image
 * @author https://petersamokhin.com
 *
 * This bot need depedencies:
 *
 * <dependency>
 *     <groupId>com.google.zxing</groupId>
 *     <artifactId>core</artifactId>
 *     <version>3.3.0</version>
 * </dependency>

 * <dependency>
 *     <groupId>com.google.zxing</groupId>
 *     <artifactId>javase</artifactId>
 *     <version>3.3.0</version>
 * </dependency>
 */
public class Main {

    public static void main(String... args) {


        Group group = new Group(151083290, "access_token");

        group.longPoll().listen(new Callback() {

            @Override
            public void onSimpleTextMessage(Message msg) {

                // Creating file if he is not exists
                File QRCodeImage = new File("template_" + msg.authorId() + ".png");
                try {
                    QRCodeImage.createNewFile();
                    Files.setPosixFilePermissions(Paths.get(QRCodeImage.getAbsolutePath()), PosixFilePermissions.fromString("rwxrwxrwx"));
                } catch (IOException ignored) {
                    System.out.println("Some error occured: " + ignored.toString() + " with file " + QRCodeImage.getAbsolutePath());
                }

                // Encode the text to the image
                QRCode qr = new QRCode();
                qr.encode(msg.getText(), QRCodeImage);

                // Send message
                new Message()
                        .from(group)
                        .to(msg.authorId())
                        .photo(QRCodeImage.getAbsolutePath())
                        .send();

                // Delete template file
                try {
                    Files.delete(Paths.get(QRCodeImage.getAbsolutePath()));
                } catch (IOException ignored) {
                }
            }

            @Override
            public void onPhotoMessage(Message msg) {

                // Creating file if he is not exists
                File QRCodeImage = new File("template_" + msg.authorId() + ".png");
                try {
                    QRCodeImage.createNewFile();
                    Files.setPosixFilePermissions(Paths.get(QRCodeImage.getAbsolutePath()), PosixFilePermissions.fromString("rwxrwxrwx"));
                } catch (IOException ignored) {
                    System.out.println("Some error occured: " + ignored.toString() + " with file " + QRCodeImage.getAbsolutePath());
                }

                // Downloading image to file
                try {
                    FileUtils.copyURLToFile(new URL(
                            msg.getBiggestPhotoUrl(msg.getPhotos())
                    ), QRCodeImage);
                } catch (IOException ignored) {
                    return;
                }

                QRCode qr = new QRCode();

                // Send message
                new Message()
                        .from(group)
                        .to(msg.authorId())
                        .text(qr.decode(QRCodeImage))
                        .send();

                // Delete template file
                try {
                    Files.delete(Paths.get(QRCodeImage.getAbsolutePath()));
                } catch (IOException ignored) {
                }
            }
        });
    }
}
