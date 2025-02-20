/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempDirectoryPath.MultibytePath
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlin.LazyThreadSafetyMode.PUBLICATION

@Throws(TempDirectoryInvalidPathException::class)
internal fun String.toPosixPath(): Utf8PosixPath = Utf8PosixPath(this)

/**
 * A byte string that passes validation as a POSIX path — it's not empty and has no bytes with a code 0.
 */
internal interface PosixPath : MultibytePath {
    @Throws(TempDirectoryInvalidPathException::class)
    fun append(path: String): PosixPath
}

internal class UnknownEncodingPosixPath(
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

internal class Utf8PosixPath(
    private val stringValue: String,
) : PosixPath {
    override val isEncodingUndefined: Boolean = false
    override val bytes: ByteString = stringValue.encodeToByteString()

    init {
        validateBasicPosixPath(bytes)
    }

    override fun append(path: String): PosixPath {
        val newPathBytes = bytes.appendPosixPath(path)
        return UnknownEncodingPosixPath(newPathBytes)
    }

    override fun asString(): String = stringValue
}

/**
 * A byte string that passes validation as a component of POSIX path — it's not empty, has no path separators
 * and bytes with a code 0.
 */
internal class PosixPathComponent private constructor(
    delegate: PosixPath,
) : PosixPath by delegate {
    init {
        validatePosixPathComponent(delegate.bytes)
    }

    internal companion object {
        internal fun PosixPath.asPathComponent(): PosixPathComponent {
            return PosixPathComponent(this)
        }
    }
}
