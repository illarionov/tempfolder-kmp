/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import kotlinx.io.bytestring.ByteString

/**
 * Represents a system-dependent file system path or a relative path segment without a specified base.
 *
 * Use [asString] to get a string representation. Note that it may throw an exception if the path cannot be
 * properly decoded.
 */
public sealed interface TempDirectoryPath {
    /**
     * Raw byte sequence of the path as returned by system calls to the file system.
     * Consider the [encoding] when converting to other formats.
     */
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
