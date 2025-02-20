/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.posix200809.PosixFileType.DIRECTORY

@Throws(TempDirectoryIOException::class)
internal fun isDirectory(
    dirFd: TempDirectoryDescriptor,
    path: PosixPath,
): Boolean = platformGetFileType(dirFd, path) == DIRECTORY

@Throws(TempDirectoryIOException::class)
internal expect fun platformGetFileType(
    dirFd: TempDirectoryDescriptor,
    path: PosixPath,
    followBaseSymlink: Boolean = false,
): PosixFileType

internal enum class PosixFileType {
    FILE,
    DIRECTORY,
    SYMLINK,
    OTHER,
}
