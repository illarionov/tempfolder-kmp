/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.TempfolderPathString.Companion.encoding
import at.released.tempfolder.path.TempfolderPathString.Encoding
import at.released.tempfolder.path.TempfolderPathString.Encoding.UTF16_LE
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.buildByteString
import kotlinx.io.bytestring.indices

@Throws(TempfolderInvalidPathException::class)
internal fun CPointer<ByteVar>.toPosixPathString(): PosixPathString {
    return UnknownEncodingPosixPathString(readNullTerminatedByteString(), false)
}

internal fun CPointer<ByteVar>.readNullTerminatedByteString(): ByteString {
    val pointer = this
    var size = 0
    while (pointer[size] != 0.toByte()) {
        size += 1
    }
    return buildByteString(size) {
        repeat(size) {
            append(pointer[it])
        }
    }
}

internal fun NativePlacement.allocNullTerminatedPath(path: PosixPathString): CArrayPointer<ByteVar> {
    val bytes: ByteString = path.bytes
    val pathNative: CArrayPointer<ByteVar> = allocArray(bytes.size + 1)
    bytes.indices.forEach { pathNative[it] = bytes[it] }
    pathNative[bytes.size] = 0
    return pathNative
}

internal fun TempfolderPathString.createNullTerminatedByteArray(): ByteArray =
    createNullTerminatedByteArray(bytes, this.encoding)

internal fun createNullTerminatedByteArray(
    bytes: ByteString,
    encoding: Encoding,
): ByteArray {
    val isWideChar = encoding == UTF16_LE
    if (isWideChar) {
        require(bytes.size % 2 == 0)
    }
    val size = bytes.size + if (isWideChar) 2 else 1
    return ByteArray(size).also {
        bytes.copyInto(it)
        it[bytes.size] = 0
        it[bytes.size + 1] = 0
    }
}
