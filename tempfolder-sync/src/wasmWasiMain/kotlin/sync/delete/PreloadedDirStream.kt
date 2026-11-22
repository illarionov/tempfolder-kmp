/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.delete

import at.released.tempfolder.TempDirectoryClosedException
import at.released.tempfolder.path.WasiPath
import at.released.tempfolder.sync.delete.DirStream.DirStreamItem
import kotlinx.io.bytestring.ByteString
import kotlin.concurrent.atomics.AtomicBoolean

internal class PreloadedDirStream(
    override val basename: WasiPath.Component,
    items: List<DirStreamItem.Entry>,
) : DirStream {
    private val items: ArrayDeque<DirStreamItem.Entry> = ArrayDeque(items)
    private val isClosed = AtomicBoolean(false)

    override fun addIgnore(name: ByteString) = Unit

    override fun readNext(): DirStreamItem {
        if (isClosed.load()) {
            throw TempDirectoryClosedException("Stream is closed")
        }
        return when (val item = items.removeFirstOrNull()) {
            null -> DirStreamItem.EndOfStream.also {
                close()
            }

            else -> item
        }
    }

    override fun close() {
        if (isClosed.exchange(true)) {
            return
        }
        items.clear()
    }
}
