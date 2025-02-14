/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.winapi

import at.released.tempfolder.TempfolderWindowsIOException
import kotlinx.cinterop.convert
import platform.windows.CloseHandle
import platform.windows.CreateFileW
import platform.windows.FILE_FLAG_BACKUP_SEMANTICS
import platform.windows.FILE_FLAG_OPEN_REPARSE_POINT
import platform.windows.FILE_READ_ATTRIBUTES
import platform.windows.FILE_SHARE_DELETE
import platform.windows.FILE_SHARE_READ
import platform.windows.FILE_SHARE_WRITE
import platform.windows.GetLastError
import platform.windows.HANDLE
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.OPEN_EXISTING

internal fun <R> withOpenFileHandle(
    path: String,
    followbaseSymlink: Boolean,
    func: (HANDLE) -> R,
): R {
    var followSymlinkAttrs = if (followbaseSymlink) {
        0U
    } else {
        FILE_FLAG_OPEN_REPARSE_POINT.toUInt()
    }

    val handle = CreateFileW(
        lpFileName = path,
        dwDesiredAccess = FILE_READ_ATTRIBUTES.convert(),
        dwShareMode = (FILE_SHARE_DELETE or FILE_SHARE_READ or FILE_SHARE_WRITE).convert(),
        lpSecurityAttributes = null,
        dwCreationDisposition = OPEN_EXISTING.convert(),
        dwFlagsAndAttributes = (FILE_FLAG_BACKUP_SEMANTICS).toUInt() or followSymlinkAttrs,
        hTemplateFile = null,
    )
    if (handle == null || handle == INVALID_HANDLE_VALUE) {
        throw TempfolderWindowsIOException("CreateFileW() failed", GetLastError())
    }

    try {
        return func(handle)
    } finally {
        CloseHandle(handle) // Ignore errors
    }
}
