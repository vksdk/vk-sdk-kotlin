package com.petersamokhin.vksdk.core.io

import com.petersamokhin.vksdk.core.error.UnsupportedActionException

/**
 * Cross-platform representation of a File
 *
 * Linux
 *
 * @property path Path to the file on disk
 */
public actual class FileOnDisk(
    public actual val path: String
) {
    /**
     * Read file contents as byte array synchronously
     */
    public actual fun readContent(): ByteArray? {
        throw UnsupportedActionException("Reading files from disk is now supported only for Darwin, JS and JVM")
    }
}