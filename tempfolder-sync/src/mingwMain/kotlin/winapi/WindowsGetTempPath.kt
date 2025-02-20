/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.TempDirectoryWindowsIOException
import at.released.tempfolder.path.WindowsPath
import at.released.tempfolder.winapi.errcode.Win32ErrorCode
import platform.windows.GetTempPathW

@Throws(TempDirectoryWindowsIOException::class)
internal fun windowsGetTempPath(): WindowsPath {
    return readPathPickBufferSize { length, buffer ->
        GetTempPathW(length, buffer)
    }.getOrElse { exception ->
        if (exception is TempDirectoryWindowsIOException) {
            throw TempDirectoryWindowsIOException(
                "Can not get temp path",
                Win32ErrorCode(exception.lastError),
                exception,
            )
        } else {
            throw TempDirectoryIOException("Can not get temp path", exception)
        }
    }
}
