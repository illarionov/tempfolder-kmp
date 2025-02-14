/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderWindowsIOException
import at.released.tempfolder.path.WindowsPathString
import platform.windows.GetFullPathNameW

@Throws(TempfolderWindowsIOException::class)
internal fun windowsGetFullPathname(path: WindowsPathString): WindowsPathString {
    val pathStr = path.asString()
    return readPathPickBufferSize { length, buffer ->
        GetFullPathNameW(pathStr, length, buffer, null)
    }.getOrElse { exception ->
        if (exception is TempfolderWindowsIOException) {
            throw TempfolderWindowsIOException("Can not get full path", exception.lastError)
        } else {
            throw TempfolderIOException("Can not get full path", exception)
        }
    }
}
