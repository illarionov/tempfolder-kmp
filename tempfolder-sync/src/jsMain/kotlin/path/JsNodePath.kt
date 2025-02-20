/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempDirectoryPath.MultibytePath
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString

internal class JsNodePath private constructor(
    private val stringValue: String,
) : MultibytePath {
    override val isEncodingUndefined: Boolean = false
    override val bytes: ByteString = stringValue.encodeToByteString()

    override fun asString(): String = stringValue

    internal companion object {
        internal fun String.toJsNodePathString(): JsNodePath = JsNodePath(this)
    }
}
