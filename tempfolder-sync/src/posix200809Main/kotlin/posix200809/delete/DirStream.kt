/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.path.PosixPath

internal sealed interface DirStream : AutoCloseable {
    val basename: PosixPath.Component

    fun readNext(): DirStreamItem
    sealed class DirStreamItem {
        data object EndOfStream : DirStreamItem()
        data class Entry(val name: PosixPath.Component, val type: DirEntryType) : DirStreamItem()
        data class Error(val error: TempDirectoryException) : DirStreamItem()
    }

    enum class DirEntryType {
        DIRECTORY,
        OTHER,
        UNKNOWN,
    }
}
