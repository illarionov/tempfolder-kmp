/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi.delete

import at.released.tempfolder.TempDirectoryWindowsIOException
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.windowsPathEndWithSpecialDirectory
import at.released.tempfolder.winapi.delete.WindowsDirectoryStream.DirectoryStreamItem
import at.released.tempfolder.winapi.delete.WindowsDirectoryStream.DirectoryStreamItem.EndOfStream
import at.released.tempfolder.winapi.delete.WindowsDirectoryStream.DirectoryStreamItem.Entry
import at.released.tempfolder.winapi.delete.WindowsDirectoryStream.DirectoryStreamItem.Error
import at.released.tempfolder.winapi.delete.WindowsDirectoryStream.Filetype.DIRECTORY
import at.released.tempfolder.winapi.delete.WindowsDirectoryStream.Filetype.FILE
import at.released.tempfolder.winapi.delete.WindowsDirectoryStream.Filetype.OTHER
import platform.windows.DeleteFileW
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.GetLastError
import platform.windows.RemoveDirectoryW
import platform.windows.SetFileAttributesW

@Throws(TempDirectoryWindowsIOException::class)
internal fun deleteDirectoryRecursively(
    path: TempDirectoryPath,
) {
    return deleteDirectoryRecursively(path.asString())
}

@Throws(TempDirectoryWindowsIOException::class)
internal fun deleteDirectoryRecursively(
    path: String,
) {
    if (windowsPathEndWithSpecialDirectory(path)) {
        throw TempDirectoryInvalidPathException("Path `$path` should be canonicalized")
    }

    BottomUpFileTreeWalker(path).use { walker ->
        while (true) {
            when (val item = walker.next()) {
                EndOfStream -> break
                is Error -> throw TempDirectoryWindowsIOException("Failed to delete file or directory")
                is Entry -> item.delete()
            }
        }
    }
}

private fun Entry.delete() {
    when (type) {
        FILE, OTHER -> if (DeleteFileW(absolutePath) == 0) {
            val lastErr = GetLastError()
            if (lastErr == ERROR_ACCESS_DENIED.toUInt()) {
                try {
                    stripReadOnlyAttribute(absolutePath)
                } catch (@Suppress("SwallowedException") _: TempDirectoryWindowsIOException) {
                    // Ignore
                }
                if (DeleteFileW(absolutePath) == 0) {
                    throw TempDirectoryWindowsIOException("Failed to delete file `$absolutePath`")
                }
            }
        }

        DIRECTORY -> if (RemoveDirectoryW(absolutePath) == 0) {
            throw TempDirectoryWindowsIOException("Failed to delete directory `$absolutePath`")
        }
    }
}

private fun stripReadOnlyAttribute(path: String) {
    if (SetFileAttributesW(path, FILE_ATTRIBUTE_NORMAL.toUInt()) == 0) {
        throw TempDirectoryWindowsIOException("Failed to set file attributes to normal on`$path`")
    }
}

private class BottomUpFileTreeWalker(
    path: String,
) : AutoCloseable {
    private val stack: ArrayDeque<Pair<WindowsDirectoryStream, Entry>> = ArrayDeque()

    init {
        val rootItem = Entry(path, "", DIRECTORY, false)
        stack.addLast(WindowsDirectoryStream(path) to rootItem)
    }

    @Suppress("LoopWithTooManyJumpStatements")
    fun next(): DirectoryStreamItem {
        var item: DirectoryStreamItem = EndOfStream
        while (stack.isNotEmpty()) {
            val (topStream: WindowsDirectoryStream, streamDirEntry: Entry) = stack.last()
            item = topStream.next()
            when (item) {
                is Error -> {
                    close()
                    break
                }

                is Entry -> when (item.type) {
                    FILE, OTHER -> break
                    DIRECTORY -> {
                        if (item.isSymlink) {
                            break
                        }
                        if (item.name != "." && item.name != "..") {
                            val newStream = WindowsDirectoryStream(item.absolutePath)
                            stack.addLast(newStream to item)
                        }
                    }
                }

                EndOfStream -> {
                    stack.removeLast()
                    item = streamDirEntry
                    break
                }
            }
        }
        return item
    }

    override fun close() {
        stack.forEach { it.first.close() }
    }
}
