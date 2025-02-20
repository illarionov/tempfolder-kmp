/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.posix200809.path.allocNullTerminatedPath
import at.released.tempfolder.posix200809.path.toPosixPath
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ByteVarOf
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import platform.posix.free

internal expect fun platformRealpath(pathNative: CPointer<ByteVar>): CPointer<ByteVar>?

@Throws(TempDirectoryIOException::class)
internal fun platformRealpath(path: PosixPath): PosixPath = memScoped {
    val pathNative: CPointer<ByteVarOf<Byte>> = allocNullTerminatedPath(path)
    val nativePath = platformRealpath(pathNative)
        ?: throw TempDirectoryIOException("Can not expand path. ${errnoDescription()}")
    val pathString = nativePath.toPosixPath()
    free(nativePath)
    return pathString
}
