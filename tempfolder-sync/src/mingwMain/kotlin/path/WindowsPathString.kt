/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempfolderPathString.WideCharPathString
import kotlinx.cinterop.CArrayPointer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.append
import kotlinx.io.bytestring.buildByteString
import platform.windows.WCHARVar

internal class WindowsPathString private constructor(
    private val wchars: String,
) : WideCharPathString {
    override val bytes: ByteString = buildByteString(wchars.length * 2) {
        wchars.forEach {
            append(it.code.toUByte())
            append((it.code ushr 8).toUByte())
        }
    }

    override fun asString(): String = wchars

    override fun toString(): String {
        return "WindowsPathString(`$wchars`)"
    }

    @Throws(TempfolderInvalidPathException::class)
    fun append(path: String): WindowsPathString {
        return windowsAppendPath(wchars, path).toWindowsPathString()
    }

    companion object {
        internal fun String.toWindowsPathString(): WindowsPathString = WindowsPathString(this)

        internal fun CArrayPointer<WCHARVar>.readWindowsPath(length: Int): WindowsPathString {
            return create(readChars(length))
        }

        @Throws(TempfolderCharacterCodingException::class)
        fun create(wchars: CharArray): WindowsPathString {
            if (wchars.isNotEmpty() &&
                (wchars[0] == 0xFEFF.toChar() || wchars[0] == 0xFFFE.toChar())
            ) {
                throw TempfolderCharacterCodingException("wchars should not contain BOM")
            }
            return WindowsPathString(wchars.concatToString())
        }
    }
}
