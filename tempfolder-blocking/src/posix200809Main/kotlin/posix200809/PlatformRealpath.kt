/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.posix200809.path.allocNullTerminatedPath
import at.released.tempfolder.posix200809.path.toPosixPathString
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ByteVarOf
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import platform.posix.free

internal expect fun platformRealpath(pathNative: CPointer<ByteVar>): CPointer<ByteVar>?

@Throws(TempfolderIOException::class)
internal fun platformRealpath(path: PosixPathString): PosixPathString = memScoped {
    val pathNative: CPointer<ByteVarOf<Byte>> = allocNullTerminatedPath(path)
    val nativePath = platformRealpath(pathNative)
        ?: throw TempfolderIOException("Can not expand path. ${errnoDescription()}")
    val pathString = nativePath.toPosixPathString()
    free(nativePath)
    return pathString
}
