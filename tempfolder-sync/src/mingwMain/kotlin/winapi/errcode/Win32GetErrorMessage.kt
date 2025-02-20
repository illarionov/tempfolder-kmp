/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi.errcode

import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.windows.FORMAT_MESSAGE_ALLOCATE_BUFFER
import platform.windows.FORMAT_MESSAGE_FROM_SYSTEM
import platform.windows.FORMAT_MESSAGE_IGNORE_INSERTS
import platform.windows.FormatMessageW
import platform.windows.LANG_ENGLISH
import platform.windows.LocalFree
import platform.windows.PWSTRVar
import platform.windows.SUBLANG_DEFAULT

internal fun Win32ErrorCode.getErrorMessage(): String? = memScoped {
    val bufferPtr: PWSTRVar = alloc<PWSTRVar>().apply {
        value = null
    }

    val flags = FORMAT_MESSAGE_ALLOCATE_BUFFER or FORMAT_MESSAGE_FROM_SYSTEM or FORMAT_MESSAGE_IGNORE_INSERTS
    val chars = FormatMessageW(
        dwFlags = flags.toUInt(),
        lpSource = null,
        dwMessageId = this@getErrorMessage.code,
        dwLanguageId = makeLangId(),
        lpBuffer = bufferPtr.ptr.reinterpret(),
        nSize = 0U,
        Arguments = null,
    )
    val errorMessage = if (chars != 0U) {
        val buf = bufferPtr.value!!
        CharArray(chars.toInt()) { idx -> buf[idx].toInt().toChar() }.concatToString().trim()
    } else {
        null
    }

    LocalFree(bufferPtr.value)
    return errorMessage
}

@Suppress("MagicNumber")
private fun makeLangId(
    primaryLanguage: Int = LANG_ENGLISH,
    sublanguage: Int = SUBLANG_DEFAULT,
): UInt = ((primaryLanguage and 0x3ff) or (sublanguage and 0x3f).shl(10)).toUInt()
