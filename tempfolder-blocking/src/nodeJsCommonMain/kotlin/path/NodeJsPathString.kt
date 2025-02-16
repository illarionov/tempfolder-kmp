/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempfolderPathString.MultibytePathString
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString

/**
 * A byte string to be used as a path in implementations of a TemporaryDirectory that use the Node.js file system
 * (the `node:fs` module).
 * Just a plain UTF-8 encoded string without any validation for now.
 */
internal class NodeJsPathString private constructor(
    private val stringValue: String,
) : MultibytePathString {
    override val isEncodingUndefined: Boolean = false
    override val bytes: ByteString = stringValue.encodeToByteString()

    override fun asString(): String = stringValue

    internal companion object {
        internal fun String.toJsNodePathString(): NodeJsPathString = NodeJsPathString(this)
    }
}
