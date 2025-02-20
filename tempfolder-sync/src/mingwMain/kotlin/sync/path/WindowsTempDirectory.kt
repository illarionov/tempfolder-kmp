/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.TempDirectoryClosedException
import at.released.tempfolder.TempDirectoryClosedException.Companion.TEMP_DIRECTORY_CLOSED_MESSAGE
import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.WindowsPath
import at.released.tempfolder.sync.TempDirectory
import at.released.tempfolder.winapi.delete.deleteDirectoryRecursively
import kotlinx.atomicfu.atomic

@Throws(TempDirectoryException::class)
public fun TempDirectory.Companion.createWindowsTempDirectory(
    block: WindowsTempDirectoryConfig.() -> Unit = {},
): TempDirectory<TempDirectoryPath> {
    val config = WindowsTempDirectoryConfig().apply(block)
    val tempRoot: WindowsPath = WindowsTempPathResolver.resolve(config.base)
    val tempDirectory = WindowsTempDirectoryCreator.createDirectory(tempRoot, config.permissions)
    return WindowsTempDirectory(tempDirectory)
}

private class WindowsTempDirectory(
    private val absolutePath: WindowsPath,
) : TempDirectory<TempDirectoryPath> {
    override var deleteOnClose: Boolean by atomic(true)
    override val root: TempDirectoryPath get() = absolutePath
    private val isClosed = atomic(false)

    override fun getAbsolutePath(): TempDirectoryPath = root

    override fun delete() {
        throwIfClosed()
        deleteDirectoryRecursively(absolutePath)
    }

    override fun append(name: String): TempDirectoryPath {
        return absolutePath.append(name)
    }

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        if (deleteOnClose) {
            deleteDirectoryRecursively(absolutePath)
        }
    }

    private fun throwIfClosed() {
        if (isClosed.value) {
            throw TempDirectoryClosedException(TEMP_DIRECTORY_CLOSED_MESSAGE)
        }
    }
}
