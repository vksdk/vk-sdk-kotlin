package com.petersamokhin.vksdk.core.api

import com.petersamokhin.vksdk.core.callback.Callback
import com.petersamokhin.vksdk.core.error.VkResponseException
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
class VkApiUploader(
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
     * @param callback Get the result
     */
    fun uploadContent(
        methodGetUploadUrl: String,
        methodSave: String,
        params: Parameters,
        items: List<UploadableContent>,
        callback: Callback<String>
    ) {
        api.uploadContent(methodGetUploadUrl, methodSave, json, params, items, callback)
    }

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
    fun uploadContent(
        methodGetUploadUrl: String,
        methodSave: String,
        params: Parameters,
        items: List<UploadableContent>
    ) = api.uploadContent(methodGetUploadUrl, methodSave, json, params, items)

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param item Item as [UploadableContent]
     * @return Attachment string, e.g. `photo12345678901_123456`
     */
    fun uploadPhotoForMessage(peerId: Int, item: UploadableContent): String? {
        val responseString = api.uploadContent(
            "photos.getMessagesUploadServer",
            "photos.saveMessagesPhoto",
            json,
            paramsOf("peer_id" to peerId),
            listOf(item)
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
     * @param item Item as [UploadableContent]
     * @param callback Get the result: Attachment string, e.g. `photo12345678901_123456`
     */
    fun uploadPhotoForMessage(peerId: Int, item: UploadableContent, callback: Callback<String>) {
        api.uploadContent(
            "photos.getMessagesUploadServer",
            "photos.saveMessagesPhoto",
            json,
            paramsOf("peer_id" to peerId),
            listOf(item),
            object: Callback<String> {
                override fun onResult(result: String) {
                    json.parseToJsonElement(result)
                        .jsonObjectOrNullSafe
                        ?.get("response")
                        ?.jsonArrayOrNullSafe
                        ?.firstOrNull()
                        ?.jsonObjectOrNullSafe
                        ?.let { photoObject ->
                            "photo${photoObject["owner_id"]?.contentOrNullSafe}_${photoObject["id"]?.contentOrNullSafe}_${photoObject["access_key"]?.contentOrNullSafe}"
                        }?.also(callback::onResult) ?: callback.onError(VkResponseException("photos.saveMessagesPhoto -> photo ids"))
                }

                override fun onError(error: Exception) = callback.onError(error)
            }
        )
    }

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param file Item as [FileOnDisk]
     * @return Attachment string, e.g. `photo12345678901_123456`
     */
    fun uploadPhotoForMessage(peerId: Int, file: FileOnDisk): String? {
        val item = UploadableContent.File(
            "photo",
            "photo.png",
            "image/png",
            file
        )

        return uploadPhotoForMessage(peerId, item)
    }

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param file Item as [FileOnDisk]
     * @param callback Get the result: Attachment string, e.g. `photo12345678901_123456`
     */
    fun uploadPhotoForMessage(peerId: Int, file: FileOnDisk, callback: Callback<String>) {
        val item = UploadableContent.File(
            "photo",
            "photo.png",
            "image/png",
            file
        )

        uploadPhotoForMessage(peerId, item, callback)
    }

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param bytes Item as [ByteArray]
     * @return Attachment string, e.g. `photo12345678901_123456`
     */
    fun uploadPhotoForMessage(peerId: Int, bytes: ByteArray): String? {
        val item = UploadableContent.Bytes(
            "photo",
            "photo.png",
            "image/png",
            bytes
        )

        return uploadPhotoForMessage(peerId, item)
    }

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param bytes Item as [ByteArray]
     * @param callback Get the result: Attachment string, e.g. `photo12345678901_123456`
     */
    fun uploadPhotoForMessage(peerId: Int, bytes: ByteArray, callback: Callback<String>) {
        val item = UploadableContent.Bytes(
            "photo",
            "photo.png",
            "image/png",
            bytes
        )

        uploadPhotoForMessage(peerId, item, callback)
    }

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param url Item by URL
     * @return Attachment string, e.g. `photo12345678901_123456`
     */
    fun uploadPhotoForMessage(peerId: Int, url: String): String? {
        val item = UploadableContent.Url(
            "photo",
            "photo.png",
            "image/png",
            url
        )

        return uploadPhotoForMessage(peerId, item)
    }

    /**
     * Upload photo to messages and get the `photo_id`
     *
     * @param peerId The conversation ID where to upload the photo
     * @param url Item by URL
     * @param callback Get the result: Attachment string, e.g. `photo12345678901_123456`
     */
    fun uploadPhotoForMessage(peerId: Int, url: String, callback: Callback<String>) {
        val item = UploadableContent.Url(
            "photo",
            "photo.png",
            "image/png",
            url
        )

        uploadPhotoForMessage(peerId, item, callback)
    }
}