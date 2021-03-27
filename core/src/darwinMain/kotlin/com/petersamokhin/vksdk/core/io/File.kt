package com.petersamokhin.vksdk.core.io

import kotlinx.cinterop.readBytes
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

/**
 * Cross-platform representation of a File
 *
 * @property path Path to the file on disk
 */
public actual class FileOnDisk(
    public actual val path: String
) {
    private val manager = NSFileManager.defaultManager

    /**
     * Read file contents as byte array synchronously
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    public actual fun readContent(): ByteArray? {
        return path
            .let { NSURL.fileURLWithPath(it).standardizedURL }
            ?.standardizedURL?.path
            ?.let {
                manager.contentsAtPath(it)?.let {
                    it.bytes?.readBytes(it.length.toInt())
                }
            }
    }
}