/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.dsl.TempDirectoryFileModeBit
import at.released.tempfolder.jsapi.nodejs.join
import at.released.tempfolder.jsapi.nodejs.mkdirSync
import at.released.tempfolder.jsapi.nodejs.toNodeJsMode
import at.released.tempfolder.sync.MAX_CREATE_DIRECTORY_ATTEMPTS
import at.released.tempfolder.sync.generateTempDirectoryName

// XXX: keep in sync with NodeJsTempDirectoryCreator
internal object WasmNodeJsTempDirectoryCreator {
    internal fun createDirectory(
        root: String,
        mode: Set<TempDirectoryFileModeBit>,
        nameGenerator: () -> String = { generateTempDirectoryName("tempfolder-") },
    ): String {
        val intMode = mode.toNodeJsMode()
        repeat(MAX_CREATE_DIRECTORY_ATTEMPTS) {
            val directoryName = nameGenerator()
            val tempDirectoryPath = join(root, directoryName)
            val directoryCreated = nodejsCreateDirectory(tempDirectoryPath, intMode)
            if (directoryCreated) {
                return tempDirectoryPath
            }
        }
        throw TempDirectoryIOException("Can not create directory: max attempts reached")
    }

    private fun nodejsCreateDirectory(path: String, mode: Int): Boolean {
        return try {
            mkdirSync(path, mode)
            true
        } catch (err: JsException) {
            if (err.message.contains("EEXIST")) {
                false
            } else {
                throw TempDirectoryIOException("mkdirSync() failed", err)
            }
        }
    }
}
