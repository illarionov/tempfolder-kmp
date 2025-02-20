/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderWindowsIOException
import at.released.tempfolder.path.WindowsPathString
import at.released.tempfolder.winapi.errcode.Win32ErrorCode
import platform.windows.GetTempPathW

@Throws(TempfolderWindowsIOException::class)
internal fun windowsGetTempPath(): WindowsPathString {
    return readPathPickBufferSize { length, buffer ->
        GetTempPathW(length, buffer)
    }.getOrElse { exception ->
        if (exception is TempfolderWindowsIOException) {
            throw TempfolderWindowsIOException(
                "Can not get temp path",
                Win32ErrorCode(exception.lastError),
                exception,
            )
        } else {
            throw TempfolderIOException("Can not get temp path", exception)
        }
    }
}
