/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempDirectoryPath.MultibytePath
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.unsafe.UnsafeByteStringApi
import kotlinx.io.bytestring.unsafe.UnsafeByteStringOperations

/**
 * A byte string that meets the basic requirements for a WASI Preview 1 File System Path:
 * 1) It is not empty.
 * 1) It does not contain any null bytes (byte value 0).
 * 1) It should be representable as a sequence of Unicode Scalar Values (USVs).
 */
internal interface WasiPath : MultibytePath {
    fun append(path: String): WasiPath

    /**
     * A WasiPath that has no path separators
     */
    interface Component : WasiPath

    private open class BaseWasiPath(
        override val bytes: ByteString,
        private val stringRepresentation: String,
    ) : WasiPath {
        override val isEncodingUndefined: Boolean = false

        override fun append(path: String): WasiPath {
            return bytes.appendPosixPath(path).toWasiPathString()
        }

        override fun asString(): String = stringRepresentation
    }

    private class BaseWasiPathComponent(
        private val delegate: WasiPath,
    ) : WasiPath by delegate, Component

    companion object {
        val WASI_PATH_CURRENT_DIRECTORY = ".".toWasiPathString().asWasiPathComponent()
        val WASI_PATH_PARENT_DIRECTORY = "..".toWasiPathString().asWasiPathComponent()

        @OptIn(UnsafeByteStringApi::class)
        fun String.toWasiPathString(): WasiPath {
            val bytes = try {
                this.encodeToByteArray(throwOnInvalidSequence = true)
            } catch (cce: CharacterCodingException) {
                throw TempDirectoryCharacterCodingException(cce)
            }
            val byteString = UnsafeByteStringOperations.wrapUnsafe(bytes)
            validateBasicPosixPath(byteString)
            return BaseWasiPath(byteString, this)
        }

        fun ByteString.toWasiPathString(): WasiPath {
            validateBasicPosixPath(this)
            val stringValue = try {
                this@toWasiPathString.toByteArray().decodeToString(throwOnInvalidSequence = true)
            } catch (ce: CharacterCodingException) {
                throw TempDirectoryCharacterCodingException(ce)
            }
            return BaseWasiPath(this, stringValue)
        }

        fun WasiPath.asWasiPathComponent(): Component {
            validatePosixPathComponent(this.bytes)
            return BaseWasiPathComponent(this)
        }

        fun ByteString.isWasiSpecialDirectory(): Boolean {
            return this == WASI_PATH_CURRENT_DIRECTORY.bytes || this == WASI_PATH_PARENT_DIRECTORY.bytes
        }
    }
}
