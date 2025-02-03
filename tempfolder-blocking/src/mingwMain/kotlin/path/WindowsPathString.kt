/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempfolderPathString.Encoding
import at.released.tempfolder.path.TempfolderPathString.Encoding.UNSPECIFIED
import at.released.tempfolder.path.TempfolderPathString.Encoding.UTF16_LE
import at.released.tempfolder.path.TempfolderPathString.Encoding.UTF8
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.append
import kotlinx.io.bytestring.buildByteString

@Throws(TempfolderCharacterCodingException::class)
internal fun TempfolderPathString.toWindowsPathString(): WindowsPathString = if (this is WindowsPathString) {
    this
} else {
    val wchars = when (this.encoding) {
        UNSPECIFIED, UTF8 -> this.asString().toCharArray()
        UTF16_LE -> this.bytes.let { bytes ->
            check(bytes.size.mod(2) == 0)
            CharArray(bytes.size / 2) {
                (bytes[it * 2].toInt() or (bytes[it * 2 + 1].toInt() shl 8)).toChar()
            }
        }
    }
    WindowsPathString(wchars)
}

@Suppress("UnusedParameter")
internal fun WindowsPathString(
    base: WindowsPathString,
    subpath: String,
): WindowsPathString {
    TODO()
}

internal class WindowsPathString(
    internal val wchars: CharArray,
) : TempfolderPathString {
    override val bytes: ByteString = buildByteString(wchars.size * 2) {
        wchars.forEach {
            append(it.code.toUByte())
            append((it.code ushr 8).toUByte())
        }
    }
    override val encoding: Encoding = UTF16_LE

    init {
        if (wchars.isNotEmpty()) {
            check(wchars[0] != 0xFEFF.toChar() && wchars[0] != 0xFFFE.toChar()) {
                "wchars should not contain BOM"
            }
        }
    }

    override fun asString(): String {
        return CharArray(wchars.size) { wchars[it].code.toChar() }.concatToString()
    }
}
