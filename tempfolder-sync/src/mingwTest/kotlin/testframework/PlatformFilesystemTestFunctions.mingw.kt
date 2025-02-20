/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.TempDirectoryWindowsIOException
import at.released.tempfolder.dsl.TempDirectoryFileModeBit
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.WindowsPath
import at.released.tempfolder.path.WindowsPath.Companion.toWindowsPathString
import at.released.tempfolder.path.windowsAppendPath
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType.NOT_SPECIFIED
import at.released.tempfolder.testframework.winapi.windowsGetFileAttributeTagInfo
import at.released.tempfolder.winapi.windowsGetFullPathname
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.isEmpty
import platform.windows.CREATE_NEW
import platform.windows.CloseHandle
import platform.windows.CreateDirectoryW
import platform.windows.CreateFileW
import platform.windows.CreateSymbolicLinkW
import platform.windows.DWORDVar
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_PRIVILEGE_NOT_HELD
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.FILE_FLAG_OPEN_REPARSE_POINT
import platform.windows.GENERIC_WRITE
import platform.windows.GetLastError
import platform.windows.HANDLE
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE
import platform.windows.WriteFile

internal actual val platformFilesystem: PlatformFilesystemTestFunctions get() = WindowsFilesystemTestFunctions

@Suppress("TooManyFunctions")
internal object WindowsFilesystemTestFunctions : PlatformFilesystemTestFunctions {
    override val isPosixFileModeSupported: Boolean get() = false
    override val isSymlinkSupported: Boolean get() = true
    override val pathSeparator: Char get() = '\\'

    private val SymlinkType.mask: UInt
        get() = when (this) {
            SymlinkType.SYMLINK_TO_FILE, NOT_SPECIFIED -> 0
            SymlinkType.SYMLINK_TO_DIRECTORY -> platform.windows.SYMBOLIC_LINK_FLAG_DIRECTORY
        }.toUInt()

    override fun joinPath(base: TempDirectoryPath, append: String): TempDirectoryPath {
        return windowsAppendPath(base.asString(), append).toWindowsPathString()
    }

    override fun isDirectory(path: TempDirectoryPath, followBasenameSymlink: Boolean): Boolean {
        return windowsGetFileAttributeTagInfo(path.asString(), followBasenameSymlink).let {
            it.fileAttributes.isDirectory && !it.fileAttributes.isSymlinkOrReparsePoint
        }
    }

    override fun isFile(path: TempDirectoryPath, followBasenameSymlink: Boolean): Boolean {
        return windowsGetFileAttributeTagInfo(path.asString(), followBasenameSymlink).let {
            !it.fileAttributes.isDirectory && !it.fileAttributes.isSymlinkOrReparsePoint
        }
    }

    override fun isSymlink(path: TempDirectoryPath): Boolean {
        return windowsGetFileAttributeTagInfo(path.asString()).isSymlink
    }

    override fun isExists(path: TempDirectoryPath, followBasenameSymlink: Boolean): Boolean {
        return try {
            windowsGetFileAttributeTagInfo(path.asString(), followBasenameSymlink)
            true
        } catch (iw: TempDirectoryWindowsIOException) {
            if (iw.lastError == ERROR_FILE_NOT_FOUND.toUInt()) {
                false
            } else {
                throw iw
            }
        }
    }

    override fun isSamePathAs(path1: TempDirectoryPath, path2: TempDirectoryPath): Boolean {
        val fullPath1 = windowsGetFullPathname(path1.toWindowsPathString())
        val fullPath2 = windowsGetFullPathname(path2.toWindowsPathString())
        return fullPath1 == fullPath2
    }

    override fun getFileMode(path: TempDirectoryPath, followBasenameSymlink: Boolean): Set<TempDirectoryFileModeBit> {
        error("Not yet implemented")
    }

    override fun createFile(path: TempDirectoryPath, mode: Set<TempDirectoryFileModeBit>, content: ByteString) {
        val handle = CreateFileW(
            lpFileName = path.asString(),
            dwDesiredAccess = GENERIC_WRITE.convert(),
            dwShareMode = 0U,
            lpSecurityAttributes = null,
            dwCreationDisposition = CREATE_NEW.convert(),
            dwFlagsAndAttributes = (FILE_ATTRIBUTE_NORMAL or FILE_FLAG_OPEN_REPARSE_POINT).convert(),
            hTemplateFile = null,
        )
        if (handle == null || handle == INVALID_HANDLE_VALUE) {
            throw TempDirectoryWindowsIOException("CreateFileW() failed")
        }
        try {
            writeContentToFile(handle, content)
        } finally {
            CloseHandle(handle) // Ignore errors
        }
    }

    private fun writeContentToFile(handle: HANDLE, content: ByteString) {
        if (content.isEmpty()) {
            return
        }
        memScoped {
            val bytesWritten: DWORDVar = alloc()
            val writeResult = content.toByteArray().usePinned {
                WriteFile(handle, it.addressOf(0), content.size.convert(), bytesWritten.ptr, null)
            }
            if (writeResult == 0) {
                throw TempDirectoryWindowsIOException("WriteFile() failed")
            }
            if (bytesWritten.value.toInt() != content.size) {
                throw TempDirectoryIOException("Failed to write ${content.size} bytes. Written: ${bytesWritten.value}")
            }
        }
    }

    override fun createDirectory(path: TempDirectoryPath, mode: Set<TempDirectoryFileModeBit>) {
        val error = CreateDirectoryW(path.asString(), null)
        if (error == 0) {
            throw TempDirectoryWindowsIOException("CreateDirectoryW() failed")
        }
    }

    override fun createSymlink(oldPath: String, newPath: TempDirectoryPath, type: SymlinkType) {
        val flags: UInt = type.mask
        if (CreateSymbolicLinkW(newPath.asString(), oldPath, type.mask).toInt() == 0) {
            val lastError = GetLastError()
            if (lastError == ERROR_PRIVILEGE_NOT_HELD.toUInt()) {
                val newFlags = flags or SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE.toUInt()
                if (CreateSymbolicLinkW(newPath.asString(), oldPath, newFlags).toInt() == 0) {
                    throw TempDirectoryWindowsIOException("CreateSymbolicLinkW() failed")
                }
            } else {
                throw TempDirectoryWindowsIOException("CreateSymbolicLinkW() failed")
            }
        }
    }

    private fun TempDirectoryPath.toWindowsPathString(): WindowsPath = if (this is WindowsPath) {
        this
    } else {
        this.asString().toWindowsPathString()
    }
}
