package com.petersamokhin.bots.sdk.clients;

import com.petersamokhin.bots.sdk.utils.Connection;
import okhttp3.MediaType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.regex.Pattern;

/**
 * Group client, that contains important methods to work with groups
 */
public class Group extends Client {

    /**
     * Default constructor
     *
     * @param id           User or group id
     * @param access_token Access token key
     */
    public Group(Integer id, String access_token) {
        super(id, access_token);
    }

    /**
     * Default constructor
     *
     * @param access_token Access token key
     */
    public Group(String access_token) {
        super(access_token);
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
     *
     * @return VK response
     */
    public JSONObject uploadCover(Object... params) {

        String cover;
        Integer groupId;
        String accessToken;

        System.out.println(params.length);

        switch (params.length) {
            case 0:
                return new JSONObject();

            case 1: {

                if (this.getId() == null || this.getId() < 0)
                    return new JSONObject();

                groupId = this.getId();
                cover = String.valueOf(params[0]);
                accessToken = this.getAccessToken();
                break;
            }

            case 2: {
                groupId = (Integer) params[0];
                cover = String.valueOf(params[1]);
                accessToken = this.getAccessToken();
                break;
            }

            case 3: {
                groupId = (Integer) params[0];
                accessToken = String.valueOf(params[1]);
                cover = String.valueOf(params[2]);
                break;
            }

            default: {
                return new JSONObject().put("response", "Some error occured, cover not uploaded.");
            }
        }

        // we can upload cover only by file from disk or url
        if (cover.endsWith(".png") || cover.endsWith(".jpg") || cover.endsWith(".gif") || cover.endsWith(".jpeg")) {

            File templatePhoto;
            String template_name = "template_cover." + FilenameUtils.getExtension(cover);
            boolean photoFromUrl = false;
            // from url
            if (Pattern.matches("https?://.+", cover)) {

                try {
                    templatePhoto = new File(template_name);
                    System.out.println(templatePhoto.createNewFile());
                    Files.setPosixFilePermissions(Paths.get(templatePhoto.getAbsolutePath()), PosixFilePermissions.fromString("rwxrwxrwx"));
                    FileUtils.copyURLToFile(new URL(cover), templatePhoto, 5000, 5000);

                    photoFromUrl = true;
                } catch (IOException ignored) {
                    System.out.println("[Message.java:141] IOException when downloading file " + cover + " : " + ignored.toString());
                    return new JSONObject().put("response", "Some error occured, cover not uploaded.");
                }
            } else {
                templatePhoto = new File(cover);
            }

            String getUploadServerQuery = "https://api.vk.com/method/photos.getOwnerCoverPhotoUploadServer?group_id=" + groupId + "&crop_x=0&crop_y=0&crop_x2=1590&crop_y2=400&access_token=" + accessToken + "&v=5.64";

            JSONObject getUploadServerResponse = Connection.getRequestResponse(getUploadServerQuery);

            String coverUploadUrl = getUploadServerResponse.getJSONObject("response").getString("upload_url");

            String responseOfUploadingPhotoToVk = Connection.getFileUploadAnswerOfVK(
                    coverUploadUrl,
                    "photo",
                    MediaType.parse("image/*"),
                    templatePhoto
            );

            responseOfUploadingPhotoToVk = (responseOfUploadingPhotoToVk != null && responseOfUploadingPhotoToVk.length() > 2) ? responseOfUploadingPhotoToVk : "{}";

            JSONObject response = new JSONObject(responseOfUploadingPhotoToVk);

            if (photoFromUrl) {
                try {
                    Files.delete(Paths.get(templatePhoto.getAbsolutePath()));
                } catch (IOException ignored) {
                }
            }

            if (response.has("hash") && response.has("photo")) {

                String hashField = response.getString("hash");
                String photoField = response.getString("photo");

                String save_cover_query = "https://api.vk.com/method/photos.saveOwnerCoverPhoto?hash=" + hashField + "&photo=" + photoField + "&access_token=" + accessToken + "&v=5.67";

                JSONObject saveCoverResponse = Connection.getRequestResponse(save_cover_query);

                return saveCoverResponse;
            }
        }

        return new JSONObject().put("response", "Some error occured, cover not uploaded.");
    }
}