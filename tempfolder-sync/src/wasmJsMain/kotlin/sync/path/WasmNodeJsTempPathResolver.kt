/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.jsapi.nodejs.realpathSync
import at.released.tempfolder.jsapi.nodejs.tmpdir

// XXX: keep in sync with NodeJsTempPathResolver
internal object WasmNodeJsTempPathResolver {
    internal fun resolve(parent: NodeTempDirectoryBase): String {
        val path = when (parent) {
            NodeTempDirectoryBase.Auto -> tmpdir()
            is NodeTempDirectoryBase.Path -> parent.path
        }
        try {
            return realpathSync(path)
        } catch (@Suppress("TooGenericExceptionCaught") ie: Exception) {
            throw TempDirectoryIOException("realpathSync() failed", ie)
        }
    }
}
