/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.TempfolderIOException
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSItemReplacementDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

internal actual fun createPlatformTempFolder(namePrefix: String): TempFolder = AppleTempFolder.create()

public class AppleTempFolder private constructor(
    private val fileManager: NSFileManager,
    public val url: NSURL,
) : TempFolder {
    override val path: String
        get() = requireNotNull(url.path) {
            "url.path is null"
        }

    override fun delete() {
        if (!fileManager.removeItemAtURL(url, null)) {
            throw TempfolderIOException("Can not remove $url")
        }
    }

    override fun resolve(name: String): String = requireNotNull(url.URLByAppendingPathComponent(name)?.path) {
        "Can not resolve `$url` with appended path `$name`"
    }

    public companion object {
        @OptIn(BetaInteropApi::class)
        public fun create(
            fileManager: NSFileManager = NSFileManager.defaultManager,
        ): AppleTempFolder {
            val tempUrl: NSURL = memScoped {
                val error: ObjCObjectVar<NSError?> = alloc()
                val cacheDirectory = fileManager.URLForDirectory(
                    directory = NSCachesDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = error.ptr,
                ) ?: throw TempfolderIOException("Can not get cache directory: ${error.value}")
                fileManager.URLForDirectory(
                    directory = NSItemReplacementDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = cacheDirectory,
                    create = true,
                    error = error.ptr,
                ) ?: throw TempfolderIOException("Can not create temporarily directory: ${error.value}")
            }
            return AppleTempFolder(fileManager, tempUrl)
        }
    }
}
