/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempDirectoryPath.MultibytePath
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * A byte string that passes validation as a POSIX path — it's not empty and has no bytes with a code 0.
 */
internal interface PosixPath : MultibytePath {
    @Throws(TempDirectoryInvalidPathException::class)
    fun append(path: String): PosixPath

    /**
     * A PosixPath that has no path separators
     */
    interface Component : PosixPath

    private class UnknownEncodingPosixPath(
        override val bytes: ByteString,
        override val isEncodingUndefined: Boolean = true,
    ) : PosixPath {
        private val stringRepresentation: Result<String> by lazy(PUBLICATION) {
            try {
                Result.success(bytes.toByteArray().decodeToString(throwOnInvalidSequence = true))
            } catch (ce: CharacterCodingException) {
                Result.failure(TempDirectoryCharacterCodingException(ce))
            }
        }

        init {
            validateBasicPosixPath(bytes)
        }

        @Throws(TempDirectoryInvalidPathException::class)
        override fun append(path: String): UnknownEncodingPosixPath {
            val newPathBytes = bytes.appendPosixPath(path)
            return UnknownEncodingPosixPath(newPathBytes)
        }

        override fun asString(): String = stringRepresentation.getOrThrow()
    }

    private class Utf8PosixPath(
        private val stringValue: String,
    ) : PosixPath {
        override val isEncodingUndefined: Boolean = false
        override val bytes: ByteString = stringValue.encodeToByteString()

        override fun append(path: String): PosixPath {
            val newPathBytes = bytes.appendPosixPath(path)
            return UnknownEncodingPosixPath(newPathBytes)
        }

        override fun asString(): String = stringValue
    }

    private class PosixPathComponent(
        private val delegate: PosixPath,
    ) : PosixPath by delegate, Component

    companion object {
        fun ByteString.toPosixPath(): PosixPath {
            validateBasicPosixPath(this)
            return UnknownEncodingPosixPath(this)
        }

        @Throws(TempDirectoryInvalidPathException::class)
        internal fun String.toPosixPath(): PosixPath {
            validateBasicPosixPath(this.encodeToByteString())
            return Utf8PosixPath(this)
        }

        internal fun PosixPath.asPathComponent(): PosixPath.Component {
            validatePosixPathComponent(this.bytes)
            return PosixPathComponent(this)
        }
    }
}
