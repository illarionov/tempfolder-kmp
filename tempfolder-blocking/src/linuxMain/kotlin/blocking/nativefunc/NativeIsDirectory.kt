/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.nativefunc

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderNativeIOException
import at.released.tempfolder.TempfolderPosixFileDescriptor
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.allocNullTerminatedPath
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.platform.linux.AT_SYMLINK_NOFOLLOW
import at.released.tempfolder.platform.linux.fstatat
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.S_IFDIR
import platform.posix.S_IFMT
import platform.posix.errno
import platform.posix.stat

@Throws(TempfolderIOException::class)
internal fun isDirectory(
    dirFd: TempfolderPosixFileDescriptor,
    name: PosixPathString,
): Boolean = memScoped {
    val statbuf: stat = alloc()
    val nameBytes = allocNullTerminatedPath(name)
    val resultCode = fstatat(dirFd.fd, nameBytes, statbuf.ptr, AT_SYMLINK_NOFOLLOW)
    if (resultCode == -1) {
        throw TempfolderNativeIOException(errno, "Can not stat `${name.asStringOrDescription()}`")
    }
    return (statbuf.st_mode.toInt() and S_IFMT) == S_IFDIR
}
