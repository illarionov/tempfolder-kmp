/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.blocking.Tempfolder
import at.released.tempfolder.blocking.generateTempDirectoryName
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.path.isAbsolute
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor.Companion.CURRENT_WORKING_DIRECTORY
import at.released.tempfolder.posix200809.delete.deleteRecursively
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.getRealPath
import at.released.tempfolder.posix200809.platformUnlinkDirectory
import at.released.tempfolder.posix200809.toPosixMode
import kotlinx.atomicfu.atomic

public class LinuxTempfolder private constructor(
    private val parentDirfd: TempfolderPosixFileDescriptor,
    private val directoryPathname: PosixPathString,
    override val root: TempfolderPosixFileDescriptor,
) : Tempfolder<TempfolderPosixFileDescriptor> {
    private val isClosed = atomic(false)
    override var deleteOnClose: Boolean by atomic(true)
    private val rootPath: Result<PosixPathString> by lazy {
        if (parentDirfd == CURRENT_WORKING_DIRECTORY && directoryPathname.isAbsolute()) {
            Result.success(directoryPathname)
        } else {
            runCatching(root::getRealPath)
        }
    }

    override fun getAbsolutePath(): TempfolderPathString {
        throwIfClosed()
        return rootPath.getOrThrow()
    }

    override fun delete() {
        // Delete content of directory
        deleteRecursively(root)
        // Try to delete directory itself
        val unlinkDirectoryError = platformUnlinkDirectory(parentDirfd, directoryPathname)
        if (unlinkDirectoryError != 0) {
            throw TempfolderNativeIOException(
                unlinkDirectoryError,
                "Unable to remove temp directory `${directoryPathname.asStringOrDescription()}`. ${errnoDescription()}",
            )
        }
    }

    @Throws(TempfolderIOException::class, TempfolderInvalidPathException::class)
    override fun resolve(name: String): TempfolderPathString {
        return rootPath.getOrThrow().append(name)
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
        @Throws(TempfolderException::class)
        public operator fun invoke(
            block: LinuxTempfolderConfig.() -> Unit,
        ): LinuxTempfolder {
            val config = LinuxTempfolderConfig().apply(block)
            val coordinates = createTempfolder(
                parent = config.base,
                mode = config.permissions.toPosixMode(),
                advisoryLock = config.advisoryLock,
                nameGenerator = { generateTempDirectoryName(config.prefix) },
            )
            return LinuxTempfolder(
                parentDirfd = coordinates.parentDirfd,
                directoryPathname = coordinates.directoryPathname,
                root = coordinates.directoryDescriptor,
            )
        }
    }
}
