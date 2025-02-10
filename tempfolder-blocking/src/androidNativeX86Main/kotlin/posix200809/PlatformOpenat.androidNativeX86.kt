/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.platform.androidnative.RESOLVE_BENEATH
import at.released.tempfolder.platform.androidnative.SYS_openat2
import at.released.tempfolder.platform.androidnative.open_how
import at.released.tempfolder.posix200809.path.allocNullTerminatedPath
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.posix.errno
import platform.posix.memset
import platform.posix.syscall

@Throws(TempfolderNativeIOException::class)
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal actual fun platformOpenAt(
    dirfd: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    mode: UInt,
    flags: ULong,
    resolveBeneath: Boolean,
): TempfolderPosixFileDescriptor {
    val resolveFlags = if (resolveBeneath) RESOLVE_BENEATH.toULong() else 0UL
    val newFd = memScoped {
        val pathBytes = allocNullTerminatedPath(path)
        val openHow: open_how = alloc<open_how> {
            memset(ptr, 0, sizeOf<open_how>().convert())
            this.flags = flags
            this.mode = mode.toULong()
            this.resolve = resolveFlags
        }
        syscall(
            SYS_openat2.convert(),
            dirfd.androidFd,
            pathBytes,
            openHow.ptr,
            sizeOf<open_how>().toULong(),
        )
    }
    if (newFd != -1) {
        return newFd.asFileDescriptor()
    } else {
        throw TempfolderNativeIOException(errno, "openat2() failed. ${errnoDescription()}")
    }
}
