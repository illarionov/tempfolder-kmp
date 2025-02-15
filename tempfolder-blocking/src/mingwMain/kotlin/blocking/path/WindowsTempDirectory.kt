/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.path

import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderException
import at.released.tempfolder.blocking.Tempfolder
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.WindowsPathString
import at.released.tempfolder.winapi.delete.deleteDirectoryRecursively
import kotlinx.atomicfu.atomic

@Throws(TempfolderException::class)
public fun Tempfolder.Companion.createWindowsTempDirectory(
    block: WindowsTempDirectoryConfig.() -> Unit = {},
): Tempfolder<TempfolderPathString> {
    val config = WindowsTempDirectoryConfig().apply(block)
    val tempRoot: WindowsPathString = WindowsTempPathResolver.resolve(config.base)
    val tempDirectory = WindowsTempDirectoryCreator.createDirectory(tempRoot, config.permissions)
    return WindowsTempDirectory(tempDirectory)
}

private class WindowsTempDirectory(
    private val absolutePath: WindowsPathString,
) : Tempfolder<TempfolderPathString> {
    override var deleteOnClose: Boolean by atomic(true)
    override val root: TempfolderPathString get() = absolutePath
    private val isClosed = atomic(false)

    override fun getAbsolutePath(): TempfolderPathString = root

    override fun delete() {
        throwIfClosed()
        deleteDirectoryRecursively(absolutePath)
    }

    override fun resolve(name: String): TempfolderPathString {
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
            throw TempfolderClosedException(TEMPFOLDER_CLOSED_MESSAGE)
        }
    }
}
