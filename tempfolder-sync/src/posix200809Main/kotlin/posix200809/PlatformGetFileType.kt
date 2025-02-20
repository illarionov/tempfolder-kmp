/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.posix200809.PosixFileType.DIRECTORY

@Throws(TempfolderIOException::class)
internal fun isDirectory(
    dirFd: TempDirectoryDescriptor,
    path: PosixPathString,
): Boolean = platformGetFileType(dirFd, path) == DIRECTORY

@Throws(TempfolderIOException::class)
internal expect fun platformGetFileType(
    dirFd: TempDirectoryDescriptor,
    path: PosixPathString,
    followBaseSymlink: Boolean = false,
): PosixFileType

internal enum class PosixFileType {
    FILE,
    DIRECTORY,
    SYMLINK,
    OTHER,
}
