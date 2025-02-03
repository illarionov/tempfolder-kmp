/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderWindowsIOException
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.WindowsPathString
import kotlinx.atomicfu.atomic

public class WindowsPathTempfolder private constructor(
    private val absolutePath: WindowsPathString,
) : Tempfolder<TempfolderPathString> {
    override var deleteOnClose: Boolean by atomic(false)
    override val root: TempfolderPathString get() = absolutePath
    private val isClosed = atomic(false)

    override fun getAbsolutePath(): TempfolderPathString = root

    override fun delete() {
        throwIfClosed()
        deleteDirectoryRecursively(absolutePath)
    }

    override fun resolve(name: String): TempfolderPathString {
        return WindowsPathString(absolutePath, name)
    }

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        if (deleteOnClose) {
            delete()
        }
    }

    private fun throwIfClosed() {
        if (isClosed.value) {
            throw TempfolderClosedException(TEMPFOLDER_CLOSED_MESSAGE)
        }
    }

    public companion object {
        private const val MAX_ATTEMPTS = 100

        public fun create(
            namePrefix: String,
        ): WindowsPathTempfolder {
            val tempPath = resolveTempRoot()
            repeat(MAX_ATTEMPTS) {
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
