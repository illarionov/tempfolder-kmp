/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.TempfolderWindowsIOException
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import platform.windows.GetLastError
import platform.windows.GetTempPathW
import platform.windows.MAX_PATH
import platform.windows.WCHARVar

private const val MAX_ATTEMPTS = 100

@Throws(TempfolderWindowsIOException::class)
internal fun resolveTempRoot(): String {
    val path = getTempRoot()
    createDirectory(path)
    return path
}

@Throws(TempfolderWindowsIOException::class)
private fun getTempRoot(): String {
    var length = MAX_PATH
    repeat(MAX_ATTEMPTS) {
        when (val result = getTempPath(length)) {
            is GetTempPathResult.Success -> return result.path
            is GetTempPathResult.Error -> throw getTempPathToWindowsIoException(result.lastError)
            is GetTempPathResult.BufferToSmall -> length = result.requiredSize + 1
        }
    }
    throw TempfolderWindowsIOException("Can not create temp path, max attempts reached")
}

private fun getTempPath(length: Int): GetTempPathResult = memScoped {
    val buffer: CArrayPointer<WCHARVar> = allocArray(length)
    val charsCopied: Int = GetTempPathW(length.toUInt(), buffer).toInt()
    when {
        charsCopied == 0 -> GetTempPathResult.Error(GetLastError())
        charsCopied <= length -> {
            val pathCharArray = CharArray(charsCopied) { index ->
                buffer[index].toInt().toChar()
            }
            val path = pathCharArray.concatToString()
            GetTempPathResult.Success(path)
        }

        else -> GetTempPathResult.BufferToSmall(charsCopied)
    }
}

private fun getTempPathToWindowsIoException(lastError: UInt): TempfolderWindowsIOException {
    return TempfolderWindowsIOException(
        "Windows error. Code: 0x${lastError.toString(16).padStart(8, '0')}`",
        lastError,
    )
}

private sealed class GetTempPathResult {
    class Error(val lastError: UInt) : GetTempPathResult()
    class BufferToSmall(val requiredSize: Int) : GetTempPathResult()
    class Success(val path: String) : GetTempPathResult()
}
