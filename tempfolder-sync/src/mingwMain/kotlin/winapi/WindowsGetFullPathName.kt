/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.TempDirectoryWindowsIOException
import at.released.tempfolder.path.WindowsPath
import at.released.tempfolder.winapi.errcode.Win32ErrorCode
import platform.windows.GetFullPathNameW

@Throws(TempDirectoryWindowsIOException::class)
internal fun windowsGetFullPathname(path: WindowsPath): WindowsPath {
    val pathStr = path.asString()
    return readPathPickBufferSize { length, buffer ->
        GetFullPathNameW(pathStr, length, buffer, null)
    }.getOrElse { exception ->
        if (exception is TempDirectoryWindowsIOException) {
            throw TempDirectoryWindowsIOException(
                "Can not get full path",
                Win32ErrorCode(exception.lastError),
                exception,
            )
        } else {
            throw TempDirectoryIOException("Can not get full path", exception)
        }
    }
}
