/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.platform.linux.AT_SYMLINK_NOFOLLOW
import at.released.tempfolder.platform.linux.fstatat
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.path.allocNullTerminatedPath
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.errno
import platform.posix.stat

internal actual fun nativeGetFileMode(
    dirFd: TempDirectoryDescriptor,
    path: PosixPathString,
    followBaseSymlink: Boolean,
): UInt = memScoped {
    val statbuf: stat = alloc()
    val nameBytes = allocNullTerminatedPath(path)
    val resultCode = fstatat(
        dirFd.fd,
        nameBytes,
        statbuf.ptr,
        if (followBaseSymlink) 0 else AT_SYMLINK_NOFOLLOW,
    )
    if (resultCode == -1) {
        throw TempfolderNativeIOException(errno, "Can not stat `${path.asStringOrDescription()}`")
    }
    return statbuf.st_mode and 0xfffU
}
