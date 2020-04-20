package com.petersamokhin.vksdk.core.io

/**
 * Cross-platform representation of a File
 *
 * Currently supported only for Darwin and JVM
 */
expect class FileOnDisk {

    /**
     * Path to the file on disk
     */
    val path: String

    /**
     * Read file contents as byte array synchronously
     */
    fun readContent(): ByteArray?
}