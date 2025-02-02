/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempfolderPathString.Encoding
import at.released.tempfolder.path.TempfolderPathString.Encoding.UTF8
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlin.LazyThreadSafetyMode.PUBLICATION

internal fun String.asPathString(): TempfolderPathString = UnicodePathString(this)

private class UnicodePathString(
    private val stringValue: String,
) : TempfolderPathString {
    override val bytes: ByteString by lazy(PUBLICATION) {
        stringValue.encodeToByteString()
    }
    override val encoding: Encoding = UTF8
    override fun asString(): String = stringValue
}
