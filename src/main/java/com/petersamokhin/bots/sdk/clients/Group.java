package com.petersamokhin.bots.sdk.clients;

import com.petersamokhin.bots.sdk.utils.Connection;
import okhttp3.MediaType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.regex.Pattern;

/**
 * Group client, that contains important methods to work with groups
 */
public class Group extends Client {

    public Group(Integer id, String access_token) {
        super(id, access_token);
    }

    /**
     * Check are users in group
     *
     * @param id IDs of users
     */
    public boolean isMember(Object id) {
        return api().call("groups.isMember", "group_id", getId().toString().replace("-", ""), "user_id", id).getInt("response") == 1;
    }

    /**
     * Upload group cover by file from url or from disk
     * @param cover Path to file
     * @return VK response
     */
    public JSONObject uploadCover(String cover) {

        // we can upload cover only by file from disk or url
        if (cover.endsWith(".png") || cover.endsWith(".jpg") || cover.endsWith(".gif") || cover.endsWith(".jpeg")) {

            File template_photo;
            String template_name = "template_cover." + FilenameUtils.getExtension(cover);
            System.out.println(template_name);
            boolean photoFromUrl = false;
            // from url
            if (Pattern.matches("https?://.+", cover)) {

                try {
                    template_photo = new File(template_name);
                    System.out.println(template_photo.createNewFile());
                    Files.setPosixFilePermissions(Paths.get(template_photo.getAbsolutePath()), PosixFilePermissions.fromString("rwxrwxrwx"));
                    FileUtils.copyURLToFile(new URL(cover), template_photo, 5000, 5000);

                    photoFromUrl = true;
                } catch (IOException ignored) {
                    System.out.println("[Message.java:141] IOException when downloading file " + cover + " : " + ignored.toString());
                    return new JSONObject();
                }
            } else {
                template_photo = new File(cover);
            }

            // Берем сервак
            String get_upload_server_query = "https://api.vk.com/method/photos.getOwnerCoverPhotoUploadServer?group_id=" + getId() + "&crop_x=0&crop_y=0&crop_x2=1590&crop_y2=400&access_token=" + getAccessToken() + "&v=5.64";

            JSONObject getUploadServerResponse = Connection.getRequestResponse(get_upload_server_query);

            // Получаем адрес для загрузки фотачки
            String cover_upload_url = getUploadServerResponse.getJSONObject("response").getString("upload_url");

            // Загружаем
            String response_string = Connection.getFileUploadAnswerOfVK(
                    cover_upload_url,
                    "photo",
                    MediaType.parse("image/*"),
                    template_photo
            );

            response_string = (response_string != null && response_string.length() > 2) ? response_string : "{}";

            JSONObject response = new JSONObject(response_string);

            if (photoFromUrl) {
                try { Files.delete(Paths.get(template_photo.getAbsolutePath()));} catch (IOException ignored) {}
            }

            if (response.has("hash") && response.has("photo")) {

                // Берём своё
                String hash_field = response.getString("hash");
                String photo_field = response.getString("photo");

                // Uploading!
                String save_cover_query = "https://api.vk.com/method/photos.saveOwnerCoverPhoto?hash=" + hash_field + "&photo=" + photo_field + "&access_token=" + getAccessToken() + "&v=5.64";

                return new JSONObject(Connection.getRequestResponse(save_cover_query));
            }
        }

        return new JSONObject();
    }
}