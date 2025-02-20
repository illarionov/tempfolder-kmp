/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.delete

import at.released.tempfolder.TempfolderException
import at.released.tempfolder.path.WasiPathString
import kotlinx.io.bytestring.ByteString

internal sealed interface DirStream : AutoCloseable {
    val basename: WasiPathString.Component

    fun addIgnore(name: ByteString)

    fun readNext(): DirStreamItem

    sealed class DirStreamItem {
        data object EndOfStream : DirStreamItem()
        data class Entry(val name: ByteString, val isDirectory: Boolean) : DirStreamItem()
        data class Error(val error: TempfolderException) : DirStreamItem()
    }
}
