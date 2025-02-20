/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi.delete

import at.released.tempfolder.path.windowsAppendPath
import at.released.tempfolder.winapi.delete.WindowsDirectoryStream.DirectoryStreamItem.Entry
import at.released.tempfolder.winapi.delete.WindowsDirectoryStream.DirectoryStreamItem.Error
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf16
import platform.windows.ERROR_NO_MORE_FILES
import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_REPARSE_POINT
import platform.windows.FindClose
import platform.windows.FindFirstFileExW
import platform.windows.FindNextFileW
import platform.windows.GetLastError
import platform.windows.HANDLE
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.IO_REPARSE_TAG_SYMLINK
import platform.windows.WIN32_FIND_DATAW
import platform.windows._FINDEX_INFO_LEVELS
import platform.windows._FINDEX_SEARCH_OPS

internal class WindowsDirectoryStream(
    private val path: String,
) : AutoCloseable {
    private var childItemsHandle: HANDLE? = null
    private val findData: WIN32_FIND_DATAW = nativeHeap.alloc()
    private var isClosed: Boolean = false

    fun next(): DirectoryStreamItem {
        check(!isClosed)
        val lastError: UInt
        if (childItemsHandle == null) {
            val childPattern = windowsAppendPath(path, "*")
            childItemsHandle = FindFirstFileExW(
                lpFileName = childPattern,
                fInfoLevelId = _FINDEX_INFO_LEVELS.FindExInfoBasic,
                lpFindFileData = findData.ptr,
                fSearchOp = _FINDEX_SEARCH_OPS.FindExSearchNameMatch,
                lpSearchFilter = null,
                dwAdditionalFlags = 0U,
            )
            lastError = if (childItemsHandle == INVALID_HANDLE_VALUE) {
                GetLastError()
            } else {
                0U
            }
        } else {
            lastError = if (FindNextFileW(childItemsHandle, findData.ptr) == 0) {
                GetLastError()
            } else {
                0U
            }
        }
        return when (lastError) {
            0U -> findData.toFileItem(path)
            ERROR_NO_MORE_FILES.toUInt() -> {
                close()
                DirectoryStreamItem.EndOfStream
            }

            else -> {
                close()
                Error(path, lastError)
            }
        }
    }

    private fun WIN32_FIND_DATAW.toFileItem(rootPath: String): Entry {
        val fileName = cFileName.toKStringFromUtf16()
        val attributesInt = dwFileAttributes.toInt()
        val fileType = when {
            attributesInt and FILE_ATTRIBUTE_DIRECTORY == FILE_ATTRIBUTE_DIRECTORY -> Filetype.DIRECTORY
            attributesInt and FILE_ATTRIBUTE_REPARSE_POINT == FILE_ATTRIBUTE_REPARSE_POINT &&
                    (dwReserved0 != IO_REPARSE_TAG_SYMLINK) -> Filetype.OTHER

            else -> Filetype.FILE
        }
        val isSymlink = attributesInt and FILE_ATTRIBUTE_REPARSE_POINT == FILE_ATTRIBUTE_REPARSE_POINT &&
                (dwReserved0 == IO_REPARSE_TAG_SYMLINK)

        return Entry(rootPath, fileName, fileType, isSymlink)
    }

    override fun close() {
        if (isClosed) {
            return
        }
        isClosed = true

        nativeHeap.free(findData.rawPtr)

        if (childItemsHandle != null) {
            FindClose(childItemsHandle) // Ignore error
        }
    }

    enum class Filetype {
        FILE,
        DIRECTORY,
        OTHER,
    }

    sealed class DirectoryStreamItem {
        data object EndOfStream : DirectoryStreamItem()

        data class Entry(
            val rootDir: String,
            val name: String,
            val type: Filetype,
            val isSymlink: Boolean,
        ) : DirectoryStreamItem() {
            val absolutePath: String get() = windowsAppendPath(rootDir, name)
        }

        data class Error(
            val rootDir: String,
            val lastError: UInt,
        ) : DirectoryStreamItem()
    }
}
