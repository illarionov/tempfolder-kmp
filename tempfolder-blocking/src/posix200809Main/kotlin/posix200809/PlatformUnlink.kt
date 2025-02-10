/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.posix200809.path.allocNullTerminatedPath
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import platform.posix.errno

internal expect fun platformUnlinkat(dirfd: Int, path: CPointer<ByteVar>, removeDirectory: Boolean): Int

internal fun platformUnlinkFile(dirfd: TempfolderPosixFileDescriptor, path: PosixPathString): Int =
    unlink(dirfd, path, false)

internal fun platformUnlinkDirectory(dirfd: TempfolderPosixFileDescriptor, path: PosixPathString): Int =
    unlink(dirfd, path, true)

private fun unlink(
    dirfd: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    removeDirectory: Boolean,
): Int {
    val resultCode = memScoped {
        val pathBytes = allocNullTerminatedPath(path)
        platformUnlinkat(dirfd.fd, pathBytes, removeDirectory)
    }
    return if (resultCode != -1) {
        0
    } else {
        errno
    }
}
