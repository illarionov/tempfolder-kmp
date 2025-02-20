/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.wasip1

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.sync.delete.DirStream.DirStreamItem.Entry
import at.released.tempfolder.wasip1.type.Filetype
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.withScopedMemoryAllocator

internal const val DEFAULT_READ_DIR_BUFFER_SIZE = 512 * 1024 * 1024
internal const val DIRENT_PACKED_SIZE = 24

internal fun wasiReadDirOrThrow(
    dirFd: TempDirectoryDescriptor,
    buffserSize: Int = DEFAULT_READ_DIR_BUFFER_SIZE,
): ReadDirPage = withScopedMemoryAllocator { allocator ->
    val bytesStoredPtr = allocator.allocateS32()
    val buffer = allocator.allocate(buffserSize)

    // The cookie parameter is not used because cookie-based paging cannot be implemented correctly
    // and does not work on most WASI Preview 1 implementations.
    wasiFdReaddir(dirFd.fd, buffer.address, buffserSize.toUInt(), 0UL, bytesStoredPtr.address)
        .throwIoExceptionOnError("fd_readdir() failed")

    val bytesStored = bytesStoredPtr.loadInt()
    val isFull = bytesStored < buffserSize
    val entries = readPageEntries(buffer, bytesStored)
    return ReadDirPage(entries, isFull)
}

@Suppress("MagicNumber", "LoopWithTooManyJumpStatements")
private fun readPageEntries(buffer: Pointer, buffserSize: Int): List<Entry> {
    val entries: MutableList<Entry> = mutableListOf()
    var offset = 0
    while (offset < buffserSize) {
        if (offset + DIRENT_PACKED_SIZE > buffserSize) {
            break
        }
        val dNamlen = (buffer + offset + 16).loadInt()
        val dType = (buffer + offset + 20).loadByte().toInt().let {
            Filetype.fromCode(it) ?: throw TempDirectoryIOException("Invalid file type code `$it`")
        }

        val entrySize = DIRENT_PACKED_SIZE + dNamlen
        if (offset + entrySize > buffserSize) {
            break
        }

        val name = (buffer + offset + DIRENT_PACKED_SIZE).loadByteString(dNamlen)
        entries += Entry(name, isDirectory = dType == Filetype.DIRECTORY)
        offset += entrySize
    }
    return entries
}

internal class ReadDirPage(
    val entries: List<Entry>,
    val isFull: Boolean,
)
