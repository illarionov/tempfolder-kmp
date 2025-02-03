/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempfolderPathString.Encoding
import at.released.tempfolder.path.TempfolderPathString.Encoding.UNSPECIFIED
import kotlinx.io.bytestring.ByteString
import kotlin.LazyThreadSafetyMode.PUBLICATION

internal class PosixPathString(
    override val bytes: ByteString,
    override val encoding: Encoding = UNSPECIFIED,
) : TempfolderPathString {
    private val stringRepresentation: Result<String> by lazy(PUBLICATION) {
        kotlin.runCatching {
            bytes.toByteArray().decodeToString(throwOnInvalidSequence = true)
        }
    }

    internal fun appendPosixPath(path: String): PosixPathString {
        val newPathBytes = bytes.appendPosixPath(path)
        return PosixPathString(newPathBytes)
    }

    init {
        validateBasicPosixPath(bytes)
    }

    override fun asString(): String = stringRepresentation.getOrThrow()
}
