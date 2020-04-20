package com.petersamokhin.vksdk.core.io

import com.petersamokhin.vksdk.core.error.UnsupportedActionException

/**
 * Cross-platform representation of a File
 *
 * @property path Path to the file on disk
 */
actual class FileOnDisk(
    actual val path: String
) {
    /**
     * Read file contents as byte array synchronously
     */
    actual fun readContent(): ByteArray? {
        throw UnsupportedActionException("Reading files from disk is now supported only for Darwin and JVM")
    }
}