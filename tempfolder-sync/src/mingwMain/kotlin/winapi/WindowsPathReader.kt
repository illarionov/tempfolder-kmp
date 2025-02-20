/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi

import at.released.tempfolder.TempDirectoryWindowsIOException
import at.released.tempfolder.path.WindowsPath
import at.released.tempfolder.path.WindowsPath.Companion.readWindowsPath
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import platform.windows.MAX_PATH
import platform.windows.WCHARVar
import kotlin.Result.Companion.failure

private const val MAX_ATTEMPTS = 100
private const val WIN32_ERROR_BUFFER_OVERFLOW = 0x6FU

internal inline fun readPathPickBufferSize(
    crossinline readPathFunc: (length: UInt, buffer: CArrayPointer<WCHARVar>) -> UInt,
): Result<WindowsPath> {
    var length = MAX_PATH
    repeat(MAX_ATTEMPTS) {
        memScoped {
            val buffer: CArrayPointer<WCHARVar> = allocArray(length)
            val charsRequired: Int = readPathFunc(length.toUInt(), buffer).toInt()
            when {
                charsRequired == 0 -> return failure(TempDirectoryWindowsIOException("Read path failed"))
                charsRequired <= length -> return kotlin.runCatching { buffer.readWindowsPath(charsRequired) }
                else -> length = charsRequired + 1
            }
        }
    }
    return failure(TempDirectoryWindowsIOException(WIN32_ERROR_BUFFER_OVERFLOW))
}
