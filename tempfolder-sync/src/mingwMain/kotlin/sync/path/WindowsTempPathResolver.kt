/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.WindowsPath
import at.released.tempfolder.path.WindowsPath.Companion.toWindowsPathString
import at.released.tempfolder.sync.path.TempDirectoryWindowsBase.Auto
import at.released.tempfolder.sync.path.TempDirectoryWindowsBase.Path
import at.released.tempfolder.winapi.windowsGetFullPathname
import at.released.tempfolder.winapi.windowsGetTempPath

internal object WindowsTempPathResolver {
    @Throws(TempDirectoryIOException::class)
    internal fun resolve(parent: TempDirectoryWindowsBase): WindowsPath {
        val path = when (parent) {
            Auto -> windowsGetTempPath()
            is Path -> parent.path.toWindowsPathString()
        }
        return windowsGetFullPathname(path)
    }
}
