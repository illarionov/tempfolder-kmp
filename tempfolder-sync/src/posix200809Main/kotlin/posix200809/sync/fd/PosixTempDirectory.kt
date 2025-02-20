/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.sync.fd

import at.released.tempfolder.TempDirectoryClosedException
import at.released.tempfolder.TempDirectoryClosedException.Companion.TEMP_DIRECTORY_CLOSED_MESSAGE
import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryDescriptor.Companion.CURRENT_WORKING_DIRECTORY
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.path.isAbsolute
import at.released.tempfolder.posix200809.TempDirectoryNativeIOException
import at.released.tempfolder.posix200809.delete.deleteRecursively
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.getRealPath
import at.released.tempfolder.posix200809.platformUnlinkDirectory
import at.released.tempfolder.sync.TempDirectory
import kotlinx.atomicfu.atomic

internal class PosixTempDirectory internal constructor(
    private val parentDirfd: TempDirectoryDescriptor,
    private val directoryPathname: PosixPath,
    override val root: TempDirectoryDescriptor,
) : TempDirectory<TempDirectoryDescriptor> {
    private val isClosed = atomic(false)
    override var deleteOnClose: Boolean by atomic(true)
    private val rootPath: Result<PosixPath> by lazy {
        if (parentDirfd == CURRENT_WORKING_DIRECTORY && directoryPathname.isAbsolute()) {
            Result.success(directoryPathname)
        } else {
            runCatching(root::getRealPath)
        }
    }

    override fun getAbsolutePath(): TempDirectoryPath {
        throwIfClosed()
        return rootPath.getOrThrow()
    }

    override fun delete() {
        throwIfClosed()
        deleteUnprotected(root)
    }

    @Throws(TempDirectoryIOException::class, TempDirectoryInvalidPathException::class)
    override fun append(name: String): TempDirectoryPath {
        return rootPath.getOrThrow().append(name)
    }

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        if (deleteOnClose) {
            deleteUnprotected(root)
        }
    }

    private fun deleteUnprotected(root: TempDirectoryDescriptor) {
        // Delete content of directory
        deleteRecursively(root)
        // Try to delete directory itself
        val unlinkDirectoryError = platformUnlinkDirectory(parentDirfd, directoryPathname)
        if (unlinkDirectoryError != 0) {
            throw TempDirectoryNativeIOException(
                unlinkDirectoryError,
                "Unable to remove temp directory `${directoryPathname.asStringOrDescription()}`. ${errnoDescription()}",
            )
        }
    }

    private fun throwIfClosed() {
        if (isClosed.value) {
            throw TempDirectoryClosedException(TEMP_DIRECTORY_CLOSED_MESSAGE)
        }
    }
}
