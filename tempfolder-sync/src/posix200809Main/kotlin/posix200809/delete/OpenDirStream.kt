/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.path.PosixPathComponent
import at.released.tempfolder.path.PosixPathComponent.Companion.asPathComponent
import at.released.tempfolder.path.TempDirectoryInvalidCharacterException
import at.released.tempfolder.posix200809.PlatformDirent
import at.released.tempfolder.posix200809.TempDirectoryNativeIOException
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.path.toPosixPath
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import platform.posix.DT_DIR
import platform.posix.DT_UNKNOWN
import platform.posix.dirent
import platform.posix.errno
import platform.posix.set_posix_errno

internal class OpenDirStream<D>(
    private val direntApi: PlatformDirent<D>,
    private val dirHandle: D,
    val dirfd: TempDirectoryDescriptor,
    override val basename: PosixPathComponent,
) : DirStream {
    override fun close() {
        if (direntApi.closedir(dirHandle) == -1) {
            throw TempDirectoryNativeIOException(
                errno,
                "Can not close directory descriptor. ${errnoDescription()}`",
            )
        }
    }

    override fun readNext(): DirStreamItem {
        set_posix_errno(0)
        val dirent: CPointer<dirent>? = direntApi.readdir(dirHandle)
        return when {
            dirent != null -> try {
                DirStreamItem.Entry(
                    name = dirent[0].d_name.toPosixPath().asPathComponent(),
                    type = when (dirent[0].d_type.toInt()) {
                        DT_DIR -> DirStream.DirEntryType.DIRECTORY
                        DT_UNKNOWN -> DirStream.DirEntryType.UNKNOWN
                        else -> DirStream.DirEntryType.OTHER
                    },
                )
            } catch (ce: TempDirectoryInvalidCharacterException) {
                DirStreamItem.Error(ce)
            }

            errno == 0 -> DirStreamItem.EndOfStream
            else -> DirStreamItem.Error(
                TempDirectoryNativeIOException(errno, "Can not read directory. ${errnoDescription(errno)}"),
            )
        }
    }
}
