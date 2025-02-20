/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.posix200809.path.allocNullTerminatedPath
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import platform.posix.errno

internal expect fun platformUnlinkat(dirfd: Int, path: CPointer<ByteVar>, removeDirectory: Boolean): Int

internal fun platformUnlinkFile(dirfd: TempDirectoryDescriptor, path: PosixPath): Int =
    unlink(dirfd, path, false)

internal fun platformUnlinkDirectory(dirfd: TempDirectoryDescriptor, path: PosixPath): Int =
    unlink(dirfd, path, true)

private fun unlink(
    dirfd: TempDirectoryDescriptor,
    path: PosixPath,
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
