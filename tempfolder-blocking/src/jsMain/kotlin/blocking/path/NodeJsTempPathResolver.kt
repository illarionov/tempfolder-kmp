/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.path

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.jsapi.nodejs.realpathSync
import at.released.tempfolder.jsapi.nodejs.tmpdir

internal object NodeJsTempPathResolver {
    internal fun resolve(parent: NodeJsTempBase): String {
        val path = when (parent) {
            NodeJsTempBase.Auto -> tmpdir()
            is NodeJsTempBase.Path -> parent.path
        }
        try {
            return realpathSync(path)
        } catch (@Suppress("TooGenericExceptionCaught") ie: Exception) {
            throw TempfolderIOException("realpathSync() failed", ie)
        }
    }
}
