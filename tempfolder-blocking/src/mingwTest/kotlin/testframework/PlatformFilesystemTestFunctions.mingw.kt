/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderWindowsIOException
import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.WindowsPathString
import at.released.tempfolder.path.WindowsPathString.Companion.toWindowsPathString
import at.released.tempfolder.path.windowsAppendPath
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType.NOT_SPECIFIED
import at.released.tempfolder.testframework.winapi.FileStatInfo.Companion.isSymlink
import at.released.tempfolder.testframework.winapi.windowsGetFileStatInfo
import at.released.tempfolder.winapi.windowsGetFullPathname
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.io.bytestring.ByteString
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

internal object WindowsFilesystemTestFunctions : PlatformFilesystemTestFunctions {
    override val isPosixFileModeSupported: Boolean get() = false
    override val isSymlinkSupported: Boolean get() = true
    override val pathSeparator: Char get() = '\\'

    override fun resolvePath(base: TempfolderPathString, append: String): TempfolderPathString {
        return windowsAppendPath(base.asString(), append).toWindowsPathString()
    }

    override fun isDirectory(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        check(!followBasenameSymlink) { "Following symlinks not implemented" }
        return windowsGetFileStatInfo(path.asString()).let {
            it.fileAttributes.isDirectory && !it.fileAttributes.isSymlinkOrReparsePoint
        }
    }

    override fun isFile(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return windowsGetFileStatInfo(path.asString()).let {
            !it.fileAttributes.isDirectory && !it.fileAttributes.isSymlinkOrReparsePoint
        }
    }

    override fun isSymlink(path: TempfolderPathString): Boolean {
        return windowsGetFileStatInfo(path.asString()).isSymlink
    }

    override fun isExists(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return try {
            windowsGetFileStatInfo(path.asString())
            true
        } catch (iw: TempfolderWindowsIOException) {
            if (iw.lastError == ERROR_FILE_NOT_FOUND.toUInt()) {
                false
            } else {
                throw iw
            }
        }
    }

    override fun isSamePathAs(path1: TempfolderPathString, path2: TempfolderPathString): Boolean {
        val fullPath1 = windowsGetFullPathname(path1.toWindowsPathString())
        val fullPath2 = windowsGetFullPathname(path2.toWindowsPathString())
        return fullPath1 == fullPath2
    }

    override fun getFileMode(path: TempfolderPathString, followBasenameSymlink: Boolean): Set<TempfolderFileModeBit> {
        error("Not yet implemented")
    }

    override fun createFile(path: TempfolderPathString, mode: Set<TempfolderFileModeBit>, content: ByteString) {
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
            throw TempfolderWindowsIOException("CreateFileW() failed", GetLastError())
        }
        try {
            memScoped {
                val bytesWritten = alloc<DWORDVar>()
                val writeResult = content.toByteArray().usePinned {
                    WriteFile(handle, it.addressOf(0), content.size.convert(), bytesWritten.ptr, null)
                }
                if (writeResult == 0) {
                    throw TempfolderWindowsIOException("WriteFile() failed", GetLastError())
                }
                if (bytesWritten.value.toInt() != content.size) {
                    throw TempfolderIOException("Failed to write ${content.size} bytes. Written: ${bytesWritten.value}")
                }
            }
        } finally {
            CloseHandle(handle) // Ignore errors
        }
    }

    override fun createDirectory(path: TempfolderPathString, mode: Set<TempfolderFileModeBit>) {
        // TODO: mode
        val error = CreateDirectoryW(path.asString(), null)
        if (error == 0) {
            throw TempfolderWindowsIOException("CreateDirectoryW() failed", GetLastError())
        }
    }

    override fun createSymlink(oldPath: String, newPath: TempfolderPathString, type: SymlinkType) {
        val flags: UInt = type.mask
        if (CreateSymbolicLinkW(newPath.asString(), oldPath, type.mask).toInt() == 0) {
            val lastError = GetLastError()
            if (lastError == ERROR_PRIVILEGE_NOT_HELD.toUInt()) {
                val newFlags = flags or SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE.toUInt()
                if (CreateSymbolicLinkW(newPath.asString(), oldPath, newFlags).toInt() == 0) {
                    throw TempfolderWindowsIOException("CreateSymbolicLinkW() failed", GetLastError())
                }
            } else {
                throw TempfolderWindowsIOException("CreateSymbolicLinkW() failed", GetLastError())
            }
        }
    }

    private val SymlinkType.mask: UInt
        get() = when (this) {
            SymlinkType.SYMLINK_TO_FILE, NOT_SPECIFIED -> 0
            SymlinkType.SYMLINK_TO_DIRECTORY -> platform.windows.SYMBOLIC_LINK_FLAG_DIRECTORY
        }.toUInt()


    private fun TempfolderPathString.toWindowsPathString(): WindowsPathString = if (this is WindowsPathString) {
        this
    } else {
        this.asString().toWindowsPathString()
    }

    private fun <R> withOpenFileHandle(
        func: (HANDLE) -> R
    ): R {

    }
}
