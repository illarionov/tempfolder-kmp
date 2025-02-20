/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi

import at.released.tempfolder.TempfolderWindowsIOException
import at.released.tempfolder.path.WindowsPathString
import at.released.tempfolder.winapi.errcode.Win32ErrorCode
import platform.windows.CreateDirectoryW
import platform.windows.ERROR_ALREADY_EXISTS
import platform.windows.ERROR_PATH_NOT_FOUND

@Throws(TempfolderWindowsIOException::class)
internal fun windowsCreateDirectory(path: WindowsPathString): Boolean {
    if (CreateDirectoryW(path.asString(), null) != 0) {
        return true
    }

    val lastError = Win32ErrorCode.last()
    if (lastError.code == ERROR_ALREADY_EXISTS.toUInt()) {
        return false
    } else {
        throw createDirectoryToWindowsIoException(lastError)
    }
}

private fun createDirectoryToWindowsIoException(
    lastError: Win32ErrorCode,
): TempfolderWindowsIOException = when (lastError.code.toInt()) {
    ERROR_ALREADY_EXISTS -> TempfolderWindowsIOException("Path already exists", lastError)
    ERROR_PATH_NOT_FOUND -> TempfolderWindowsIOException("Failed to resolve intermediate directories")
    else -> TempfolderWindowsIOException("Windows error.")
}
