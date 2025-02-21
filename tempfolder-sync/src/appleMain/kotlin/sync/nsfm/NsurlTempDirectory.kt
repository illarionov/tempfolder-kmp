/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(BetaInteropApi::class)

package at.released.tempfolder.sync.nsfm

import at.released.tempfolder.TempDirectoryClosedException
import at.released.tempfolder.TempDirectoryClosedException.Companion.TEMP_DIRECTORY_CLOSED_MESSAGE
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.TempDirectoryCharacterCodingException
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.toPosixPath
import at.released.tempfolder.sync.TempDirectory
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

internal class NsurlTempDirectory private constructor(
    private val fileManager: NSFileManager,
    absolutePath: NSURL,
) : TempDirectory<NSURL> {
    override var deleteOnClose: Boolean by atomic(true)
    override val root: NSURL = absolutePath
    private val isClosed = atomic(false)

    override fun getAbsolutePath(): TempDirectoryPath {
        return root.path?.toPosixPath() ?: throw TempDirectoryCharacterCodingException("Can not convert url")
    }

    override fun delete() {
        throwIfClosed()
        deleteRecursively(fileManager, root)
    }

    override fun append(name: String): TempDirectoryPath {
        return root.URLByAppendingPathComponent(name)?.path?.toPosixPath()
            ?: throw TempDirectoryInvalidPathException("Can not resolve `$root` with appended path `$name`")
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
            throw TempDirectoryClosedException(TEMP_DIRECTORY_CLOSED_MESSAGE)
        }
    }

    companion object {
        internal fun create(
            block: NsurlTempDirectoryConfig.() -> Unit = {},
        ): TempDirectory<NSURL> {
            val config = NsurlTempDirectoryConfig().apply(block)
            val tempDirectoryUrl = createAppleNsurlTempDirectory(config.fileManager, config.base)
            return NsurlTempDirectory(config.fileManager, tempDirectoryUrl)
        }

        @Throws(TempDirectoryIOException::class)
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
                throw TempDirectoryIOException("Can not remove $root: $error")
            }
        }
    }
}
