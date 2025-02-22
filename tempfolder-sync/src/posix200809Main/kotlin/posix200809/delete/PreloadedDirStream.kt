/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.TempDirectoryClosedException
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem
import kotlinx.atomicfu.atomic

internal class PreloadedDirStream(
    override val basename: PosixPath.Component,
    items: List<DirStreamItem.Entry>,
) : DirStream {
    private val items: ArrayDeque<DirStreamItem.Entry> = ArrayDeque(items)
    private var isClosed = atomic(false)

    override fun readNext(): DirStreamItem {
        if (isClosed.value) {
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
        if (isClosed.getAndSet(true)) {
            return
        }
        items.clear()
    }
}
