/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempDirectoryPath.WideCharPath
import kotlinx.cinterop.CArrayPointer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.append
import kotlinx.io.bytestring.buildByteString
import platform.windows.WCHARVar

internal class WindowsPath private constructor(
    private val wchars: String,
) : WideCharPath {
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

    @Throws(TempDirectoryInvalidPathException::class)
    fun append(path: String): WindowsPath {
        return windowsAppendPath(wchars, path).toWindowsPathString()
    }

    companion object {
        internal fun String.toWindowsPathString(): WindowsPath = WindowsPath(this)

        internal fun CArrayPointer<WCHARVar>.readWindowsPath(length: Int): WindowsPath {
            return create(readChars(length))
        }

        @Throws(TempDirectoryCharacterCodingException::class)
        fun create(wchars: CharArray): WindowsPath {
            if (wchars.isNotEmpty() &&
                (wchars[0] == 0xFEFF.toChar() || wchars[0] == 0xFFFE.toChar())
            ) {
                throw TempDirectoryCharacterCodingException("wchars should not contain BOM")
            }
            return WindowsPath(wchars.concatToString())
        }
    }
}
