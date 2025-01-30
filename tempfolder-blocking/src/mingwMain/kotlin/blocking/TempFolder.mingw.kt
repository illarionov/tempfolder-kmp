/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.TempfolderWindowsIOException

internal actual fun createPlatformTempFolder(namePrefix: String): TempFolder {
    return WindowsTempFolder.create(namePrefix)
}

public class WindowsTempFolder private constructor(
    override val path: String,
) : TempFolder {
    override fun delete() {
        deleteDirectoryRecursively(path)
    }

    override fun resolve(name: String): String = combinePath(path, name)

    public companion object {
        private const val MAX_ATTEMPTS = 100

        public fun create(
            namePrefix: String,
        ): WindowsTempFolder {
            val tempPath = resolveTempRoot()
            repeat(MAX_ATTEMPTS) {
                val tempDirectoryPath = tempPath + generateTempDirectoryName(namePrefix)
                val directoryCreated = createDirectory(tempDirectoryPath)
                if (directoryCreated) {
                    return WindowsTempFolder(tempDirectoryPath)
                }
            }
            throw TempfolderWindowsIOException("Can not create directory: max attempts reached")
        }
    }
}
