/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.WasiPath
import at.released.tempfolder.path.WasiPath.Companion.toWasiPathString
import at.released.tempfolder.wasip1.WasiPreopens

internal fun WasiPreopens.resolvePath(path: TempDirectoryPath): Pair<TempDirectoryDescriptor, WasiPath> {
    return resolvePath(path.asString().toWasiPathString())
}

internal fun WasiPreopens.resolvePath(path: WasiPath): Pair<TempDirectoryDescriptor, WasiPath> {
    val pathString = path.asString()
    val (fd, preopenPath) = rootForPath(path).firstOrNull()
        ?: throw TempDirectoryIOException("Path `$path` not resolvable")
    val relativePath = pathString.removePrefix(preopenPath.asString()).trimStart('/')
    return if (relativePath.isEmpty()) {
        fd to WasiPath.WASI_PATH_CURRENT_DIRECTORY
    } else {
        fd to relativePath.toWasiPathString()
    }
}
