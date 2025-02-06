/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.nativefunc.delete

import at.released.tempfolder.TempfolderNativeIOException
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirEntryType.DIRECTORY
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirEntryType.OTHER
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirEntryType.UNKNOWN
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirStreamItem.EndOfStream
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirStreamItem.Error
import at.released.tempfolder.blocking.nativefunc.errnoDescription
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.toPosixPathString
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import platform.posix.DIR
import platform.posix.DT_DIR
import platform.posix.DT_UNKNOWN
import platform.posix.closedir
import platform.posix.dirent
import platform.posix.errno
import platform.posix.readdir
import platform.posix.set_posix_errno

internal class DirStream(
    val dir: CPointer<DIR>,
) : AutoCloseable {
    override fun close() {
        if (closedir(dir) == -1) {
            throw TempfolderNativeIOException(
                errno,
                "Can not close directory descriptor. ${errnoDescription()}`",
            )
        }
    }

    fun readNext(): DirStreamItem = posixReadDir(dir)

    internal sealed class DirStreamItem {
        data object EndOfStream : DirStreamItem()
        data class Entry(val name: PosixPathString, val type: DirEntryType) : DirStreamItem()
        data class Error(val error: TempfolderNativeIOException) : DirStreamItem()
    }

    internal enum class DirEntryType {
        DIRECTORY,
        OTHER,
        UNKNOWN,
    }

    private companion object {
        fun posixReadDir(
            dir: CPointer<DIR>,
        ): DirStreamItem {
            set_posix_errno(0)
            val dirent: CPointer<dirent>? = readdir(dir)
            return when {
                dirent != null -> DirStreamItem.Entry(
                    name = dirent[0].d_name.toPosixPathString(),
                    type = when (dirent[0].d_type.toInt()) {
                        DT_DIR -> DIRECTORY
                        DT_UNKNOWN -> UNKNOWN
                        else -> OTHER
                    },
                )

                errno == 0 -> EndOfStream
                else -> Error(
                    TempfolderNativeIOException(errno, "Can not read directory. ${errnoDescription(errno)}"),
                )
            }
        }
    }
}
