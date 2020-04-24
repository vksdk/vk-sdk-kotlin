package com.petersamokhin.vksdk.core.io

private external fun require(module: String): dynamic

/**
 * Cross-platform representation of a File
 *
 * Currently supported only for Darwin and JVM
 */
actual data class FileOnDisk(
    /**
     * Path to the file on disk
     */
    actual val path: String
) {
    /**
     * Read file contents as byte array synchronously
     */
    actual fun readContent(): ByteArray? {
        val buffer: dynamic = require("fs").readFileSync(path)
        return js("Array").prototype.slice.call(buffer, 0).unsafeCast<ByteArray>()
    }
}