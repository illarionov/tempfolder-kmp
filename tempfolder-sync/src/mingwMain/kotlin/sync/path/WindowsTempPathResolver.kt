/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.WindowsPathString
import at.released.tempfolder.path.WindowsPathString.Companion.toWindowsPathString
import at.released.tempfolder.sync.path.WindowsTempBase.Auto
import at.released.tempfolder.sync.path.WindowsTempBase.Path
import at.released.tempfolder.winapi.windowsGetFullPathname
import at.released.tempfolder.winapi.windowsGetTempPath

internal object WindowsTempPathResolver {
    @Throws(TempfolderIOException::class)
    internal fun resolve(parent: WindowsTempBase): WindowsPathString {
        val path = when (parent) {
            Auto -> windowsGetTempPath()
            is Path -> parent.path.toWindowsPathString()
        }
        return windowsGetFullPathname(path)
    }
}
