/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.posix200809.DirP
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
import at.released.tempfolder.posix200809.asFileDescriptor
import at.released.tempfolder.posix200809.closedir
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem
import at.released.tempfolder.posix200809.dirfd
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.path.toPosixPathString
import at.released.tempfolder.posix200809.readdir
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import platform.posix.DT_DIR
import platform.posix.DT_UNKNOWN
import platform.posix.dirent
import platform.posix.errno
import platform.posix.set_posix_errno

internal class PosixDirStream(
    private val dir: DirP,
) : DirStream {
    override val dirfd: TempfolderPosixFileDescriptor = dirfd(dir).asFileDescriptor()

    override fun close() {
        if (closedir(dir) == -1) {
            throw TempfolderNativeIOException(
                errno,
                "Can not close directory descriptor. ${errnoDescription()}`",
            )
        }
    }

    override fun readNext(): DirStreamItem {
        set_posix_errno(0)
        val dirent: CPointer<dirent>? = readdir(dir)
        return when {
            dirent != null -> DirStreamItem.Entry(
                name = dirent[0].d_name.toPosixPathString(),
                type = when (dirent[0].d_type.toInt()) {
                    DT_DIR -> DirStream.DirEntryType.DIRECTORY
                    DT_UNKNOWN -> DirStream.DirEntryType.UNKNOWN
                    else -> DirStream.DirEntryType.OTHER
                },
            )

            errno == 0 -> DirStreamItem.EndOfStream
            else -> DirStreamItem.Error(
                TempfolderNativeIOException(errno, "Can not read directory. ${errnoDescription(errno)}"),
            )
        }
    }
}
