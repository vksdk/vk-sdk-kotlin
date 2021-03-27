package com.petersamokhin.vksdk.core.io

import java.io.File

/**
 * Cross-platform representation of a File
 */
public actual data class FileOnDisk(
    /**
     * Path to the file on disk
     */
    actual val path: String
) {
    private val file = File(path)

    /**
     * Read file contents as byte array synchronously
     */
    public actual fun readContent(): ByteArray? =
        file.readBytes()
}