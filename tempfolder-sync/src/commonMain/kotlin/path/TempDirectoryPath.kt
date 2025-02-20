/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import kotlinx.io.bytestring.ByteString

/**
 * An entity representing a system-dependent path in a file system or a relative path segment with no specified base.
 */
public sealed interface TempDirectoryPath {
    public val bytes: ByteString

    /**
     * Returns the path represented as a string.
     *
     * @throws TempDirectoryCharacterCodingException if the path cannot be decoded into a valid Unicode string
     */
    @Throws(TempDirectoryCharacterCodingException::class)
    public fun asString(): String

    public interface MultibytePath : TempDirectoryPath {
        public val isEncodingUndefined: Boolean
    }

    public interface WideCharPath : TempDirectoryPath

    /**
     * Encoding of the [TempDirectoryPath.bytes] payload.
     */
    public enum class Encoding {
        /**
         * Encoding is not specified (Most likely it's UTF8)
         */
        UNDEFINED,
        UTF8,
        UTF16_LE,
    }

    public companion object {
        public val TempDirectoryPath.encoding: Encoding
            get() = when (this) {
                is MultibytePath -> if (isEncodingUndefined) Encoding.UNDEFINED else Encoding.UTF8
                is WideCharPath -> Encoding.UTF16_LE
            }
    }
}
