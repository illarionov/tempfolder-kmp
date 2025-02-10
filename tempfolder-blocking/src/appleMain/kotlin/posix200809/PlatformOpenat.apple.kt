/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.platform.apple.openat
import at.released.tempfolder.posix200809.path.allocNullTerminatedPath
import kotlinx.cinterop.memScoped
import platform.posix.errno

@Throws(TempfolderNativeIOException::class)
internal actual fun platformOpenAt(
    dirfd: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    mode: UInt,
    flags: ULong,
    resolveBeneath: Boolean,
): TempfolderPosixFileDescriptor {
    val fd = memScoped {
        val pathBytes = allocNullTerminatedPath(path)
        openat(dirfd.appleFd, pathBytes, flags, mode)
    }
    if (fd != -1) {
        return fd.asFileDescriptor()
    } else {
        throw TempfolderNativeIOException(errno, "openat() failed. ${errnoDescription()}")
    }
}
