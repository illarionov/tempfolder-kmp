/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.WasiPathString
import at.released.tempfolder.path.WasiPathString.Companion.toWasiPathString
import at.released.tempfolder.wasip1.WasiPreopens

internal fun WasiPreopens.resolvePath(path: TempfolderPathString): Pair<TempDirectoryDescriptor, WasiPathString> {
    return resolvePath(path.asString().toWasiPathString())
}

internal fun WasiPreopens.resolvePath(path: WasiPathString): Pair<TempDirectoryDescriptor, WasiPathString> {
    val pathString = path.asString()
    val (fd, preopenPath) = rootForPath(path).firstOrNull()
        ?: throw TempfolderIOException("Path `$path` not resolvable")
    val relativePath = pathString.removePrefix(preopenPath.asString()).trimStart('/')
    return if (relativePath.isEmpty()) {
        fd to WasiPathString.WASI_PATH_CURRENT_DIRECTORY
    } else {
        fd to relativePath.toWasiPathString()
    }
}
