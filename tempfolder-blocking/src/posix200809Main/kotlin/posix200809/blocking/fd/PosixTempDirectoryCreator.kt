/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.blocking.fd

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.blocking.MAX_CREATE_DIRECTORY_ATTEMPTS
import at.released.tempfolder.blocking.generateTempDirectoryName
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.PosixPathStringComponent
import at.released.tempfolder.path.PosixPathStringComponent.Companion.asPathComponent
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.toPosixPathString
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor.Companion.CURRENT_WORKING_DIRECTORY
import at.released.tempfolder.posix200809.blocking.fd.PosixTempDirectoryCreator.CreateDirectoryResult.DirectoryExists
import at.released.tempfolder.posix200809.blocking.fd.PosixTempDirectoryCreator.CreateDirectoryResult.Error
import at.released.tempfolder.posix200809.blocking.fd.PosixTempDirectoryCreator.CreateDirectoryResult.Success
import at.released.tempfolder.posix200809.blocking.fd.PosixTempDirectoryCreator.ResolvedTempRoot.FileDescriptor
import at.released.tempfolder.posix200809.blocking.fd.PosixTempDirectoryCreator.ResolvedTempRoot.Path
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.nativeOpenDirectoryAt
import at.released.tempfolder.posix200809.platformMkdirat
import at.released.tempfolder.posix200809.platformUnlinkDirectory
import platform.posix.EEXIST

internal object PosixTempDirectoryCreator {
    @Throws(TempfolderIOException::class, TempfolderInvalidPathException::class)
    internal fun createDirectory(
        root: ResolvedTempRoot,
        mode: UInt = 0b000_111_000_000U,
        nameGenerator: () -> String = { generateTempDirectoryName("tempfolder-") },
    ): TempfolderCoordinates {
        val tempDirectoryFd = (1..MAX_CREATE_DIRECTORY_ATTEMPTS).firstNotNullOfOrNull {
            val directoryName = nameGenerator().toPosixPathString().asPathComponent()
            tryCreateTempfolder(root, directoryName, mode)
        }
        return tempDirectoryFd ?: throw TempfolderIOException("Can not create temp folder: max attempts reached")
    }

    @Throws(TempfolderIOException::class)
    private fun tryCreateTempfolder(
        root: ResolvedTempRoot,
        directoryName: PosixPathStringComponent,
        mode: UInt,
    ): TempfolderCoordinates? {
        val (dirFd: TempfolderPosixFileDescriptor, pathname: PosixPathString) = when (root) {
            is FileDescriptor -> root.fd to directoryName
            is Path -> CURRENT_WORKING_DIRECTORY to root.path.append(directoryName.asString())
        }

        when (val createDirectoryResult = tryCreateDirectory(dirFd, pathname, mode)) {
            DirectoryExists -> return null
            is Error -> throw createDirectoryResult.lastError
            Success -> Unit
        }

        try {
            val tempdirFd = nativeOpenDirectoryAt(dirFd, pathname, resolveBeneath = false)
            return TempfolderCoordinates(
                parentDirfd = dirFd,
                directoryPathname = pathname,
                directoryDescriptor = tempdirFd,
            )
        } catch (ie: TempfolderNativeIOException) {
            val errno = platformUnlinkDirectory(dirFd, pathname)
            if (errno != 0) {
                ie.addSuppressed(
                    TempfolderNativeIOException(errno, "Can not remove temp directory. ${errnoDescription()}"),
                )
            }
            throw ie
        }
    }

    private fun tryCreateDirectory(
        base: TempfolderPosixFileDescriptor,
        directoryName: PosixPathString,
        mode: UInt,
    ): CreateDirectoryResult {
        val mkdirResult = platformMkdirat(base, directoryName, mode)
        return CreateDirectoryResult.create(mkdirResult)
    }

    private sealed class CreateDirectoryResult {
        data object Success : CreateDirectoryResult()
        data object DirectoryExists : CreateDirectoryResult()
        class Error(val lastError: Exception) : CreateDirectoryResult()

        companion object {
            fun create(result: Int): CreateDirectoryResult = when {
                result == 0 -> Success
                result == EEXIST -> DirectoryExists
                else -> Error(
                    TempfolderNativeIOException(result, "Failed to open temp directory. ${errnoDescription(result)}"),
                )
            }
        }
    }

    internal sealed interface ResolvedTempRoot {
        value class FileDescriptor(val fd: TempfolderPosixFileDescriptor) : ResolvedTempRoot
        value class Path(val path: PosixPathString) : ResolvedTempRoot
    }

    internal class TempfolderCoordinates(
        val parentDirfd: TempfolderPosixFileDescriptor,
        val directoryPathname: PosixPathString,
        val directoryDescriptor: TempfolderPosixFileDescriptor,
    )
}
