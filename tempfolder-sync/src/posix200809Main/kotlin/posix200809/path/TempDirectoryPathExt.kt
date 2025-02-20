/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.path

import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.UnknownEncodingPosixPath
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

@Throws(TempDirectoryInvalidPathException::class)
internal fun CPointer<ByteVar>.toPosixPath(): PosixPath {
    return UnknownEncodingPosixPath(readNullTerminatedByteString(), false)
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

internal fun NativePlacement.allocNullTerminatedPath(path: PosixPath): CArrayPointer<ByteVar> {
    val bytes: ByteString = path.bytes
    val pathNative: CArrayPointer<ByteVar> = allocArray(bytes.size + 1)
    bytes.indices.forEach { pathNative[it] = bytes[it] }
    pathNative[bytes.size] = 0
    return pathNative
}
