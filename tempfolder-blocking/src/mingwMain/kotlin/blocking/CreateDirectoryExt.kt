/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.TempfolderWindowsIOException
import platform.windows.CreateDirectoryW
import platform.windows.ERROR_ALREADY_EXISTS
import platform.windows.ERROR_PATH_NOT_FOUND
import platform.windows.GetLastError

@Throws(TempfolderWindowsIOException::class)
internal fun createDirectory(path: String): Boolean {
    if (CreateDirectoryW(path, null) != 0) {
        return true
    }

    val lastError = GetLastError()
    if (lastError == ERROR_ALREADY_EXISTS.toUInt()) {
        return false
    } else {
        throw createDirectoryToWindowsIoException(lastError)
    }
}

private fun createDirectoryToWindowsIoException(
    lastError: UInt,
): TempfolderWindowsIOException = when (lastError.toInt()) {
    ERROR_ALREADY_EXISTS -> TempfolderWindowsIOException("Path already exists", lastError)
    ERROR_PATH_NOT_FOUND -> TempfolderWindowsIOException("Failed to resolve intermediate directories", lastError)
    else -> TempfolderWindowsIOException(
        "Windows error. Code: 0x${lastError.toString(16).padStart(8, '0')}`",
        lastError,
    )
}
