package com.petersamokhin.vksdk.core.model.objects

import com.petersamokhin.vksdk.core.io.FileOnDisk

sealed class UploadableContent {
    abstract val fieldName: String
    abstract val fileName: String
    abstract val mediaType: String

    data class Bytes(
        override val fieldName: String,
        override val fileName: String,
        override val mediaType: String,
        val bytes: ByteArray
    ): UploadableContent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Bytes) return false

            if (fileName != other.fileName) return false
            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = fileName.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }

    data class File(
        override val fieldName: String,
        override val fileName: String,
        override val mediaType: String,
        val file: FileOnDisk
    ): UploadableContent()

    data class Url(
        override val fieldName: String,
        override val fileName: String,
        override val mediaType: String,
        val url: String
    ): UploadableContent()
}

