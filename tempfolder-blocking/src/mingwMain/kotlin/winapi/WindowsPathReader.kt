/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi

import at.released.tempfolder.TempfolderWindowsIOException
import at.released.tempfolder.path.WindowsPathString
import at.released.tempfolder.path.WindowsPathString.Companion.readWindowsPath
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import platform.windows.MAX_PATH
import platform.windows.WCHARVar
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

private const val MAX_ATTEMPTS = 100
private const val WIN32_ERROR_BUFFER_OVERFLOW = 0x6FU

internal inline fun readPathPickBufferSize(
    crossinline readPathFunc: (length: UInt, buffer: CArrayPointer<WCHARVar>) -> UInt,
): Result<WindowsPathString> {
    var length = MAX_PATH
    repeat(MAX_ATTEMPTS) {
        memScoped {
            val buffer: CArrayPointer<WCHARVar> = allocArray(length)
            val charsRequired: Int = readPathFunc(length.toUInt(), buffer).toInt()
            when {
                charsRequired == 0 -> return failure(TempfolderWindowsIOException())
                charsRequired <= length -> kotlin.runCatching {
                    success(buffer.readWindowsPath(charsRequired))
                }

                else -> length = charsRequired + 1
            }
        }
    }
    return failure(TempfolderWindowsIOException(WIN32_ERROR_BUFFER_OVERFLOW))
}
