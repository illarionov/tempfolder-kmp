/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.delete

import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.path.WasiPath
import kotlinx.io.bytestring.ByteString

internal sealed interface DirStream : AutoCloseable {
    val basename: WasiPath.Component

    fun addIgnore(name: ByteString)

    fun readNext(): DirStreamItem

    sealed class DirStreamItem {
        data object EndOfStream : DirStreamItem()
        data class Entry(val name: ByteString, val isDirectory: Boolean) : DirStreamItem()
        data class Error(val error: TempDirectoryException) : DirStreamItem()
    }
}
