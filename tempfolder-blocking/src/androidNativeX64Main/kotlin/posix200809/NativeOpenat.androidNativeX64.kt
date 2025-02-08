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
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.posix.O_DIRECTORY
import platform.posix.O_NOFOLLOW
import platform.posix.O_NONBLOCK
import platform.posix.errno
import platform.posix.memset
import platform.posix.syscall

// XXX copy of the linux implementation
@Throws(TempfolderNativeIOException::class)
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal actual fun nativeOpenDirectoryAt(
    dirfd: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    resolveBeneath: Boolean,
): TempfolderPosixFileDescriptor {
    val resolveFlags = if (resolveBeneath) RESOLVE_BENEATH.toULong() else 0UL
    val newFd = memScoped {
        val pathBytes = allocNullTerminatedPath(path)
        val openHow: open_how = alloc<open_how> {
            memset(ptr, 0, sizeOf<open_how>().toULong())
            this.flags = (O_DIRECTORY or O_NOFOLLOW or O_NONBLOCK).toULong()
            this.mode = 0b000_111_000_000U
            this.resolve = resolveFlags
        }
        syscall(SYS_openat2.toLong(), dirfd.androidFd, pathBytes, openHow.ptr, sizeOf<open_how>().toUInt())
    }
    if (newFd != -1L) {
        return newFd.toInt().asFileDescriptor()
    } else {
        throw TempfolderNativeIOException(errno, "Can not open created directory. ${errnoDescription()}")
    }
}
