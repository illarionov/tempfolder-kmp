/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.jsapi.nodejs.realpathSync
import at.released.tempfolder.jsapi.nodejs.tmpdir

internal object NodeJsTempPathResolver {
    internal fun resolve(parent: TempDirectoryNodeBase): String {
        val path = when (parent) {
            TempDirectoryNodeBase.Auto -> tmpdir()
            is TempDirectoryNodeBase.Path -> parent.path
        }
        try {
            return realpathSync(path)
        } catch (@Suppress("TooGenericExceptionCaught") ie: Exception) {
            throw TempDirectoryIOException("realpathSync() failed", ie)
        }
    }
}
