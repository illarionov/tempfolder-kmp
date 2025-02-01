/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import kotlinx.io.bytestring.ByteString

/**
 * An entity representing a system-dependent path in a file system or a relative path segment with no specified base.
 */
public interface TempfolderPathString {
    public val bytes: ByteString
    public val encoding: Encoding

    /**
     * Returns the path represented as a string.
     *
     * @throws CharacterCodingException if the path cannot be decoded into a valid Unicode string
     */
    @Throws(CharacterCodingException::class)
    public fun asString(): String

    /**
     * Encoding of the [TempfolderPathString.bytes] payload.
     */
    public enum class Encoding {
        /**
         * Encoding is not specified (Most likely it's UTF8)
         */
        UNSPECIFIED,
        UTF8,
        UTF16_LE,
    }
}
