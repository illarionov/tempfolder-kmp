/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.nativefunc

import at.released.tempfolder.TempfolderPosixFileDescriptor
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.allocNullTerminatedPath
import at.released.tempfolder.platform.linux.AT_REMOVEDIR
import at.released.tempfolder.platform.linux.unlinkat
import kotlinx.cinterop.memScoped
import platform.posix.errno

internal fun unlinkFile(dirfd: TempfolderPosixFileDescriptor, path: PosixPathString): Int = unlink(dirfd, path, 0)

internal fun unlinkDirectory(dirfd: TempfolderPosixFileDescriptor, path: PosixPathString): Int =
    unlink(dirfd, path, AT_REMOVEDIR)

private fun unlink(
    dirfd: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    flags: Int,
): Int {
    val resultCode = memScoped {
        val pathBytes = allocNullTerminatedPath(path)
        unlinkat(dirfd.fd, pathBytes, flags)
    }
    return if (resultCode != -1) {
        0
    } else {
        errno
    }
}
