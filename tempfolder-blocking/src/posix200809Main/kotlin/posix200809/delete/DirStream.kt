/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor

internal interface DirStream : AutoCloseable {
    val dirfd: TempfolderPosixFileDescriptor

    fun readNext(): DirStreamItem
    sealed class DirStreamItem {
        data object EndOfStream : DirStreamItem()
        data class Entry(val name: PosixPathString, val type: DirEntryType) : DirStreamItem()
        data class Error(val error: TempfolderNativeIOException) : DirStreamItem()
    }

    enum class DirEntryType {
        DIRECTORY,
        OTHER,
        UNKNOWN,
    }
}
