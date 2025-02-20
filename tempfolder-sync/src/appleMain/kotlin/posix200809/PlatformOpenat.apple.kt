/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.asFileDescriptor
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.platform.apple.openat
import at.released.tempfolder.posix200809.path.allocNullTerminatedPath
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import platform.posix.errno

@Throws(TempDirectoryNativeIOException::class)
internal actual fun platformOpenAt(
    dirfd: TempDirectoryDescriptor,
    path: PosixPath,
    mode: UInt,
    flags: ULong,
    resolveBeneath: Boolean,
): TempDirectoryDescriptor {
    val fd = memScoped {
        val pathBytes = allocNullTerminatedPath(path)
        openat(dirfd.appleFd, pathBytes, flags.convert(), mode.toInt())
    }
    if (fd != -1) {
        return fd.asFileDescriptor()
    } else {
        throw TempDirectoryNativeIOException(errno, "openat() failed. ${errnoDescription()}")
    }
}
