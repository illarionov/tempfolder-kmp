/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.TempfolderCharacterCodingException
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.asPathString
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.UnsafeNumber
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

public class AppleNsurlTempfolder private constructor(
    private val fileManager: NSFileManager,
    absolutePath: NSURL,
) : Tempfolder<NSURL> {
    override var deleteOnClose: Boolean by atomic(true)
    override val root: NSURL = absolutePath
    private val isClosed = atomic(false)

    override fun getAbsolutePath(): TempfolderPathString {
        return root.path?.asPathString() ?: throw TempfolderCharacterCodingException("Can not convert url")
    }

    @OptIn(BetaInteropApi::class)
    override fun delete() {
        throwIfClosed()

        val error = memScoped {
            val cError: ObjCObjectVar<NSError?> = alloc()
            if (!fileManager.removeItemAtURL(root, null)) {
                cError.value?.let { nserror: NSError ->
                    nserror.localizedDescription // need more detailed exception?
                } ?: "Unknown error"
            } else {
                null
            }
        }

        if (error != null) {
            throw TempfolderIOException("Can not remove $root: $error")
        }
    }

    override fun resolve(name: String): TempfolderPathString {
        return root.URLByAppendingPathComponent(name)?.path?.asPathString()
            ?: throw TempfolderInvalidPathException("Can not resolve `$root` with appended path `$name`")
    }

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        if (deleteOnClose) {
            delete()
        }
    }

    private fun throwIfClosed() {
        if (isClosed.value) {
            throw TempfolderClosedException(TEMPFOLDER_CLOSED_MESSAGE)
        }
    }

    public companion object {
        @OptIn(BetaInteropApi::class, UnsafeNumber::class)
        public fun create(
            fileManager: NSFileManager = NSFileManager.defaultManager,
        ): AppleNsurlTempfolder {
            val tempUrl: NSURL = memScoped {
                val error: ObjCObjectVar<NSError?> = alloc()
                val cacheDirectory = fileManager.URLForDirectory(
                    directory = NSCachesDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = error.ptr,
                ) ?: throw TempfolderIOException("Can not get cache directory: ${error.value?.localizedDescription}")
                fileManager.URLForDirectory(
                    directory = NSItemReplacementDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = cacheDirectory,
                    create = true,
                    error = error.ptr,
                ) ?: throw TempfolderIOException(
                    "Can not create temporarily directory: ${error.value?.localizedDescription}",
                )
            }
            return AppleNsurlTempfolder(fileManager, tempUrl)
        }
    }
}
