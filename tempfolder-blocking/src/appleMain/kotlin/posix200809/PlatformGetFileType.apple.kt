/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.platform.apple.fstatat
import at.released.tempfolder.posix200809.path.allocNullTerminatedPath
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.AT_SYMLINK_NOFOLLOW
import platform.posix.S_IFDIR
import platform.posix.S_IFLNK
import platform.posix.S_IFMT
import platform.posix.S_IFREG
import platform.posix.errno
import platform.posix.stat

@Throws(TempfolderIOException::class)
internal actual fun platformGetFileType(
    dirFd: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    followBaseSymlink: Boolean,
): PosixFileType = memScoped {
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
    return when (statbuf.st_mode.toInt() and S_IFMT) {
        S_IFREG -> PosixFileType.FILE
        S_IFDIR -> PosixFileType.DIRECTORY
        S_IFLNK -> PosixFileType.SYMLINK
        else -> PosixFileType.OTHER
    }
}
