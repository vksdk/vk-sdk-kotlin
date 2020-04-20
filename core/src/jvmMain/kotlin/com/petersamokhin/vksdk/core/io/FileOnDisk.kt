package com.petersamokhin.vksdk.core.io

import java.io.File

/**
 * Cross-platform representation of a File
 */
actual data class FileOnDisk(
    /**
     * Path to the file on disk
     */
    actual val path: String
) {
    private val file = File(path)

    /**
     * Read file contents as byte array synchronously
     */
    actual fun readContent(): ByteArray? = file.readBytes()
}