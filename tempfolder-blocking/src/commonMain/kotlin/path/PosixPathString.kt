/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempfolderPathString.MultibytePathString
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlin.LazyThreadSafetyMode.PUBLICATION

@Throws(TempfolderInvalidPathException::class)
internal fun String.toPosixPathString(): Utf8PosixPathString = Utf8PosixPathString(this)

/**
 * A byte string that passes validation as a POSIX path — it's not empty and has no bytes with a code 0.
 */
internal interface PosixPathString : MultibytePathString {
    @Throws(TempfolderInvalidPathException::class)
    fun append(path: String): PosixPathString
}

internal class UnknownEncodingPosixPathString(
    override val bytes: ByteString,
    override val isEncodingUndefined: Boolean = true,
) : PosixPathString {
    private val stringRepresentation: Result<String> by lazy(PUBLICATION) {
        try {
            Result.success(bytes.toByteArray().decodeToString(throwOnInvalidSequence = true))
        } catch (ce: CharacterCodingException) {
            Result.failure(TempfolderCharacterCodingException(ce))
        }
    }

    init {
        validateBasicPosixPath(bytes)
    }

    @Throws(TempfolderInvalidPathException::class)
    override fun append(path: String): UnknownEncodingPosixPathString {
        val newPathBytes = bytes.appendPosixPath(path)
        return UnknownEncodingPosixPathString(newPathBytes)
    }

    override fun asString(): String = stringRepresentation.getOrThrow()
}

internal class Utf8PosixPathString(
    private val stringValue: String,
) : PosixPathString {
    override val isEncodingUndefined: Boolean = false
    override val bytes: ByteString = stringValue.encodeToByteString()

    init {
        validateBasicPosixPath(bytes)
    }

    override fun append(path: String): PosixPathString {
        val newPathBytes = bytes.appendPosixPath(path)
        return UnknownEncodingPosixPathString(newPathBytes)
    }

    override fun asString(): String = stringValue
}

/**
 * A byte string that passes validation as a component of POSIX path — it's not empty, has no path separators
 * and bytes with a code 0.
 */
internal class PosixPathStringComponent private constructor(
    delegate: PosixPathString,
) : MultibytePathString, PosixPathString by delegate {
    init {
        validatePosixPathComponent(delegate.bytes)
    }

    internal companion object {
        internal fun PosixPathString.asPathComponent(): PosixPathStringComponent {
            return PosixPathStringComponent(this)
        }
    }
}
