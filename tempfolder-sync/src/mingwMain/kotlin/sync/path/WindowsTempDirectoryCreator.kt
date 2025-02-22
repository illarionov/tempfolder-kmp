/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.WindowsPath
import at.released.tempfolder.sync.MAX_CREATE_DIRECTORY_ATTEMPTS
import at.released.tempfolder.sync.generateTempDirectoryName
import at.released.tempfolder.winapi.windowsCreateDirectory

internal object WindowsTempDirectoryCreator {
    @Throws(TempDirectoryIOException::class)
    internal fun createDirectory(
        root: WindowsPath,
        nameGenerator: () -> String = { generateTempDirectoryName("tempfolder-") },
    ): WindowsPath {
        repeat(MAX_CREATE_DIRECTORY_ATTEMPTS) {
            val directoryName = nameGenerator()
            val tempDirectoryPath = root.append(directoryName)
            val directoryCreated = windowsCreateDirectory(tempDirectoryPath)
            if (directoryCreated) {
                return tempDirectoryPath
            }
        }
        throw TempDirectoryIOException("Can not create directory: max attempts reached")
    }
}
