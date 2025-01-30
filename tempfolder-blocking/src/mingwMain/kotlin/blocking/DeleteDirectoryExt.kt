/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.TempfolderWindowsIOException
import at.released.tempfolder.blocking.WindowsDirectoryStream.DirectoryStreamItem
import at.released.tempfolder.blocking.WindowsDirectoryStream.DirectoryStreamItem.EndOfStream
import at.released.tempfolder.blocking.WindowsDirectoryStream.DirectoryStreamItem.Error
import at.released.tempfolder.blocking.WindowsDirectoryStream.DirectoryStreamItem.FileItem
import at.released.tempfolder.blocking.WindowsDirectoryStream.Filetype.DIRECTORY
import at.released.tempfolder.blocking.WindowsDirectoryStream.Filetype.FILE
import at.released.tempfolder.blocking.WindowsDirectoryStream.Filetype.OTHER
import platform.windows.DeleteFileW
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.GetLastError
import platform.windows.RemoveDirectoryW
import platform.windows.SetFileAttributesW

@Throws(TempfolderWindowsIOException::class)
internal fun deleteDirectoryRecursively(
    path: String,
): Unit = BottomUpFileTreeWalker(path).use { walker ->
    while (true) {
        when (val item = walker.next()) {
            EndOfStream -> break
            is Error -> throw TempfolderWindowsIOException("Failed to delete file or directory", item.lastError)
            is FileItem -> item.delete()
        }
    }
}

private fun FileItem.delete() {
    when (type) {
        FILE, OTHER -> if (DeleteFileW(absolutePath) == 0) {
            val lastErr = GetLastError()
            if (lastErr == ERROR_ACCESS_DENIED.toUInt()) {
                try {
                    stripReadOnlyAttribute(absolutePath)
                } catch (@Suppress("SwallowedException") _: TempfolderWindowsIOException) {
                    // Ignore
                }
                if (DeleteFileW(absolutePath) == 0) {
                    val newLastErr = GetLastError()
                    throw TempfolderWindowsIOException("Failed to delete file `$absolutePath`", newLastErr)
                }
            }
        }

        DIRECTORY -> if (RemoveDirectoryW(absolutePath) == 0) {
            val lastErr = GetLastError()
            throw TempfolderWindowsIOException("Failed to delete directory `$absolutePath`", lastErr)
        }
    }
}

private fun stripReadOnlyAttribute(path: String) {
    if (SetFileAttributesW(path, FILE_ATTRIBUTE_NORMAL.toUInt()) == 0) {
        val lastErr = GetLastError()
        throw TempfolderWindowsIOException("Failed to set file attributes to normal on`$path`", lastErr)
    }
}

private class BottomUpFileTreeWalker(
    path: String,
) : AutoCloseable {
    private val stack: ArrayDeque<Pair<WindowsDirectoryStream, FileItem>> = ArrayDeque()

    init {
        val rootItem = FileItem(path, "", DIRECTORY, false)
        stack.addLast(WindowsDirectoryStream(path) to rootItem)
    }

    @Suppress("LoopWithTooManyJumpStatements")
    fun next(): DirectoryStreamItem {
        var item: DirectoryStreamItem = EndOfStream
        while (stack.isNotEmpty()) {
            val (topStream, topItemInfo) = stack.last()
            item = topStream.next()
            when (item) {
                is Error -> {
                    close()
                    break
                }

                is FileItem -> when (item.type) {
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
                    item = topItemInfo
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
