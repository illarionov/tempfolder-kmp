/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.TempfolderWindowsIOException
import at.released.tempfolder.WindowsPathString
import kotlinx.atomicfu.atomic

internal actual fun createPlatformTempFolder(namePrefix: String): Tempfolder<*> {
    return WindowsPathTempfolder.create(namePrefix)
}

public class WindowsPathTempfolder private constructor(
    private val absolutePath: WindowsPathString
) : Tempfolder<TempfolderPathString> {
    override var deleteOnClose: Boolean by atomic(false)
    override val root: TempfolderPathString get() = absolutePath

    override fun getAbsolutePath(): TempfolderPathString = root

    override fun delete() {
        deleteDirectoryRecursively(absolutePath)
    }

    override fun resolve(name: String): TempfolderPathString {
        return WindowsPathString(absolutePath, name)
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    public companion object {
        private const val MAX_ATTEMPTS = 100

        public fun create(
            namePrefix: String,
        ): WindowsPathTempfolder {
            val tempPath = resolveTempRoot()
            repeat(MAX_ATTEMPTS) {
                // TODO: get absolute path of root
                val tempDirectoryPath = WindowsPathString(tempPath, generateTempDirectoryName(namePrefix))
                val directoryCreated = createDirectory(tempDirectoryPath)
                if (directoryCreated) {
                    return WindowsPathTempfolder(tempDirectoryPath)
                }
            }
            throw TempfolderWindowsIOException("Can not create directory: max attempts reached")
        }
    }
}
