package com.petersamokhin.vksdk.core.api

import com.petersamokhin.vksdk.core.http.Parameters
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.io.FileOnDisk
import com.petersamokhin.vksdk.core.model.objects.UploadableContent
import com.petersamokhin.vksdk.core.utils.contentOrNullSafe
import com.petersamokhin.vksdk.core.utils.jsonArrayOrNullSafe
import com.petersamokhin.vksdk.core.utils.jsonObjectOrNullSafe
import kotlinx.serialization.json.Json

/**
 * Uploader for photos, etc
 */
public class VkApiUploader(
    private val api: VkApi,
    private val json: Json
) {

    /**
     * Upload whatever you want wherever you can.
     *
     * Omg, vtentakle, why you can't do it by yourself...
     *
     * @param methodGetUploadUrl Where to get the upload URL, e. g. `photos.getMessagesUploadServer`
     * @param methodSave Where to save, e. g. `photos.saveMessagesPhoto`
     * @param params Parameters needed for getting the upload URL, e. g. `peer_id`
     * @param items Uploadable items, e.g. photo
     */
    public suspend fun uploadContent(
        methodGetUploadUrl: String,
        methodSave: String,
        params: Parameters,
        items: List<UploadableContent>
    ): String = api.uploadContent(methodGetUploadUrl, methodSave, json, params, items)

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param item Item as [UploadableContent]
     * @return Attachment string, e.g. `photo12345678901_123456`
     */
    public suspend fun uploadPhotoForMessage(peerId: Int, item: UploadableContent): String? {
        val responseString = api.uploadContent(
            methodGetUploadUrl = "photos.getMessagesUploadServer",
            methodSave = "photos.saveMessagesPhoto",
            json = json,
            params = paramsOf("peer_id" to peerId),
            items = listOf(item)
        )

        return json.parseToJsonElement(responseString)
            .jsonObjectOrNullSafe
            ?.get("response")
            ?.jsonArrayOrNullSafe
            ?.firstOrNull()
            ?.jsonObjectOrNullSafe
            ?.let { photoObject ->
                "photo${photoObject["owner_id"]?.contentOrNullSafe}_${photoObject["id"]?.contentOrNullSafe}_${photoObject["access_key"]?.contentOrNullSafe}"
            }
    }

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param file Item as [FileOnDisk]
     * @return Attachment string, e.g. `photo12345678901_123456`
     */
    public suspend fun uploadPhotoForMessage(peerId: Int, file: FileOnDisk): String? {
        val item = UploadableContent.File(
            fieldName = "photo",
            fileName = "photo.png",
            mediaType = "image/png",
            file = file
        )

        return uploadPhotoForMessage(peerId, item)
    }

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param bytes Item as [ByteArray]
     * @return Attachment string, e.g. `photo12345678901_123456`
     */
    public suspend fun uploadPhotoForMessage(peerId: Int, bytes: ByteArray): String? {
        val item = UploadableContent.Bytes(
            fieldName = "photo",
            fileName = "photo.png",
            mediaType = "image/png",
            bytes = bytes
        )

        return uploadPhotoForMessage(peerId, item)
    }

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param url Item by URL
     * @return Attachment string, e.g. `photo12345678901_123456`
     */
    public suspend fun uploadPhotoForMessage(peerId: Int, url: String): String? {
        val item = UploadableContent.Url(
            fieldName = "photo",
            fileName = "photo.png",
            mediaType = "image/png",
            url = url
        )

        return uploadPhotoForMessage(peerId, item)
    }
}