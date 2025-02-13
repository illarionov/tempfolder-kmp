/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(BetaInteropApi::class)

package at.released.tempfolder.blocking.nsfm

import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.blocking.Tempfolder
import at.released.tempfolder.path.TempfolderCharacterCodingException
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.toPosixPathString
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

public class NsurlTempDirectory private constructor(
    private val fileManager: NSFileManager,
    absolutePath: NSURL,
) : Tempfolder<NSURL> {
    override var deleteOnClose: Boolean by atomic(true)
    override val root: NSURL = absolutePath
    private val isClosed = atomic(false)

    override fun getAbsolutePath(): TempfolderPathString {
        return root.path?.toPosixPathString() ?: throw TempfolderCharacterCodingException("Can not convert url")
    }

    override fun delete() {
        throwIfClosed()
        deleteRecursively(fileManager, root)
    }

    override fun resolve(name: String): TempfolderPathString {
        return root.URLByAppendingPathComponent(name)?.path?.toPosixPathString()
            ?: throw TempfolderInvalidPathException("Can not resolve `$root` with appended path `$name`")
    }

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        if (deleteOnClose) {
            deleteRecursively(fileManager, root)
        }
    }

    private fun throwIfClosed() {
        if (isClosed.value) {
            throw TempfolderClosedException(TEMPFOLDER_CLOSED_MESSAGE)
        }
    }

    public companion object {
        @Throws(TempfolderIOException::class)
        public operator fun invoke(
            block: NsurlTempDirectoryConfig.() -> Unit = {},
        ): NsurlTempDirectory {
            val config = NsurlTempDirectoryConfig().apply(block)
            val tempDirectoryUrl = createAppleNsurlTempDirectory(config.fileManager, config.base)
            return NsurlTempDirectory(config.fileManager, tempDirectoryUrl)
        }

        @Throws(TempfolderIOException::class)
        private fun deleteRecursively(fileManager: NSFileManager, root: NSURL) {
            val error = memScoped {
                val cError: ObjCObjectVar<NSError?> = alloc()
                if (!fileManager.removeItemAtURL(root, cError.ptr)) {
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
    }
}
