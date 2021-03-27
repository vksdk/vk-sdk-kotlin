package com.petersamokhin.vksdk.core.io

/**
 * Cross-platform representation of a File
 *
 * Currently supported only for Darwin, JS and JVM
 */
public expect class FileOnDisk {

    /**
     * Path to the file on disk
     */
    public val path: String

    /**
     * Read file contents as byte array synchronously
     */
    public fun readContent(): ByteArray?
}