/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.sync.fd

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryDescriptor.Companion.CURRENT_WORKING_DIRECTORY
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.path.PosixPathComponent
import at.released.tempfolder.path.PosixPathComponent.Companion.asPathComponent
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.toPosixPath
import at.released.tempfolder.posix200809.TempDirectoryNativeIOException
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.nativeOpenDirectoryAt
import at.released.tempfolder.posix200809.platformMkdirat
import at.released.tempfolder.posix200809.platformUnlinkDirectory
import at.released.tempfolder.posix200809.sync.fd.PosixTempDirectoryCreator.CreateDirectoryResult.DirectoryExists
import at.released.tempfolder.posix200809.sync.fd.PosixTempDirectoryCreator.CreateDirectoryResult.Error
import at.released.tempfolder.posix200809.sync.fd.PosixTempDirectoryCreator.CreateDirectoryResult.Success
import at.released.tempfolder.posix200809.sync.fd.PosixTempRootResolver.ResolvedTempRoot
import at.released.tempfolder.posix200809.sync.fd.PosixTempRootResolver.ResolvedTempRoot.FileDescriptor
import at.released.tempfolder.posix200809.sync.fd.PosixTempRootResolver.ResolvedTempRoot.Path
import at.released.tempfolder.sync.MAX_CREATE_DIRECTORY_ATTEMPTS
import at.released.tempfolder.sync.generateTempDirectoryName
import platform.posix.EEXIST

internal object PosixTempDirectoryCreator {
    @Throws(TempDirectoryIOException::class, TempDirectoryInvalidPathException::class)
    internal fun createDirectory(
        root: ResolvedTempRoot,
        mode: UInt = 0b000_111_000_000U,
        nameGenerator: () -> String = { generateTempDirectoryName("tempfolder-") },
    ): TempfolderCoordinates {
        val tempDirectoryFd = (1..MAX_CREATE_DIRECTORY_ATTEMPTS).firstNotNullOfOrNull {
            val directoryName = nameGenerator().toPosixPath().asPathComponent()
            tryCreateTempfolder(root, directoryName, mode)
        }
        return tempDirectoryFd ?: throw TempDirectoryIOException("Can not create temp folder: max attempts reached")
    }

    @Throws(TempDirectoryIOException::class)
    private fun tryCreateTempfolder(
        root: ResolvedTempRoot,
        directoryName: PosixPathComponent,
        mode: UInt,
    ): TempfolderCoordinates? {
        val (dirFd: TempDirectoryDescriptor, pathname: PosixPath) = when (root) {
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
        } catch (ie: TempDirectoryNativeIOException) {
            val errno = platformUnlinkDirectory(dirFd, pathname)
            if (errno != 0) {
                ie.addSuppressed(
                    TempDirectoryNativeIOException(errno, "Can not remove temp directory. ${errnoDescription()}"),
                )
            }
            throw ie
        }
    }

    private fun tryCreateDirectory(
        base: TempDirectoryDescriptor,
        directoryName: PosixPath,
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
                    TempDirectoryNativeIOException(
                        result,
                        "Failed to open temp directory. ${errnoDescription(result)}",
                    ),
                )
            }
        }
    }

    internal class TempfolderCoordinates(
        val parentDirfd: TempDirectoryDescriptor,
        val directoryPathname: PosixPath,
        val directoryDescriptor: TempDirectoryDescriptor,
    )
}
