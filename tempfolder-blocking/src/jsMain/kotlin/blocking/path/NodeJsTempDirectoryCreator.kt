/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.path

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.blocking.MAX_CREATE_DIRECTORY_ATTEMPTS
import at.released.tempfolder.blocking.generateTempDirectoryName
import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.jsapi.nodejs.join
import at.released.tempfolder.jsapi.nodejs.mkdirSync
import at.released.tempfolder.jsapi.nodejs.nodeJsErrorCode
import at.released.tempfolder.jsapi.nodejs.toNodeJsMode

internal object NodeJsTempDirectoryCreator {
    internal fun createDirectory(
        root: String,
        mode: Set<TempfolderFileModeBit>,
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
        throw TempfolderIOException("Can not create directory: max attempts reached")
    }
}

private fun nodejsCreateDirectory(path: String, mode: Int): Boolean {
    return try {
        mkdirSync(path, mode)
        true
    } catch (@Suppress("TooGenericExceptionCaught") err: Throwable) {
        if (err.nodeJsErrorCode == "EEXIST") {
            false
        } else {
            throw TempfolderIOException("mkdirSync() failed", err)
        }
    }
}
