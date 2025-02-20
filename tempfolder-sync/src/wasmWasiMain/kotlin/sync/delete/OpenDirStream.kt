/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.delete

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.WasiPath
import at.released.tempfolder.path.WasiPath.Companion.WASI_PATH_CURRENT_DIRECTORY
import at.released.tempfolder.path.WasiPath.Companion.WASI_PATH_PARENT_DIRECTORY
import at.released.tempfolder.sync.delete.DirStream.DirStreamItem
import at.released.tempfolder.wasip1.DEFAULT_READ_DIR_BUFFER_SIZE
import at.released.tempfolder.wasip1.DIRENT_PACKED_SIZE
import at.released.tempfolder.wasip1.ReadDirPage
import at.released.tempfolder.wasip1.wasiCloseOrThrow
import at.released.tempfolder.wasip1.wasiReadDirOrThrow
import kotlinx.io.bytestring.ByteString

internal class OpenDirStream(
    val dirfd: TempDirectoryDescriptor,
    override val basename: WasiPath.Component,
    private val readDirOrThrow: (dirfd: TempDirectoryDescriptor, size: Int) -> ReadDirPage = ::wasiReadDirOrThrow,
    private val closeOrThrow: (dirfd: TempDirectoryDescriptor) -> Unit = ::wasiCloseOrThrow,
) : DirStream {
    private val ignoredEntries: MutableSet<ByteString> = mutableSetOf(elements = DEFAULT_IGNORES)
    private var ignoredEntrySize = defaultIgnoresSize
    private val defaultReadDirSize = DEFAULT_READ_DIR_BUFFER_SIZE - ignoredEntrySize
    private val entries: ArrayDeque<DirStreamItem.Entry> = ArrayDeque()
    private var loadComplete: Boolean = false

    override fun addIgnore(name: ByteString) {
        if (ignoredEntries.add(name)) {
            ignoredEntrySize += name.direntSize
        }
    }

    @Suppress("ReturnCount")
    override fun readNext(): DirStreamItem {
        if (entries.isNotEmpty()) {
            return entries.removeFirst()
        }

        if (loadComplete) {
            return DirStreamItem.EndOfStream
        }

        val bufferSize = defaultReadDirSize + ignoredEntrySize
        val page = try {
            readDirOrThrow(dirfd, bufferSize)
        } catch (ioe: TempDirectoryIOException) {
            loadComplete = true
            return DirStreamItem.Error(ioe)
        }

        page.entries.filterTo(entries) { !ignoredEntries.contains(it.name) }

        loadComplete = page.isFull || entries.isEmpty()

        return if (entries.isNotEmpty()) {
            entries.removeFirst()
        } else {
            DirStreamItem.EndOfStream
        }
    }

    override fun close() {
        closeOrThrow(dirfd)
    }

    private companion object {
        private val DEFAULT_IGNORES: Array<ByteString> = arrayOf(
            WASI_PATH_CURRENT_DIRECTORY.bytes,
            WASI_PATH_PARENT_DIRECTORY.bytes,
        )
        private val defaultIgnoresSize = DEFAULT_IGNORES.sumOf { it.direntSize }

        private val ByteString.direntSize: Int
            get() = DIRENT_PACKED_SIZE + size
    }
}
