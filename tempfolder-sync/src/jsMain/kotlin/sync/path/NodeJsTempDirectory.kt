/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.TempDirectoryClosedException
import at.released.tempfolder.TempDirectoryClosedException.Companion.TEMP_DIRECTORY_CLOSED_MESSAGE
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.jsapi.nodejs.RmSyncOptions
import at.released.tempfolder.jsapi.nodejs.join
import at.released.tempfolder.jsapi.nodejs.rmSync
import at.released.tempfolder.path.JsNodePath.Companion.toJsNodePathString
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.sync.TempDirectory
import kotlinx.atomicfu.atomic

internal class NodeJsTempDirectory(absolutePath: String) : TempDirectory<String> {
    override var deleteOnClose: Boolean by atomic(true)
    override val root: String = absolutePath
    private val isClosed = atomic(false)

    override fun getAbsolutePath(): TempDirectoryPath = root.toJsNodePathString()

    override fun delete() {
        throwIfClosed()
        deleteDirectoryRecursively(root)
    }

    override fun append(name: String): TempDirectoryPath {
        try {
            return join(root, name).toJsNodePathString()
        } catch (@Suppress("TooGenericExceptionCaught") err: Exception) {
            throw TempDirectoryInvalidPathException("join(`$root`, `$name`) failed", err)
        }
    }

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        if (deleteOnClose) {
            deleteDirectoryRecursively(root)
        }
    }

    private fun throwIfClosed() {
        if (isClosed.value) {
            throw TempDirectoryClosedException(TEMP_DIRECTORY_CLOSED_MESSAGE)
        }
    }

    private companion object {
        fun deleteDirectoryRecursively(path: String) {
            try {
                rmSync(path, RmSyncOptions { recursive = true })
            } catch (@Suppress("TooGenericExceptionCaught") err: Exception) {
                throw TempDirectoryIOException("rmSync() failed", err)
            }
        }

        private inline fun RmSyncOptions(
            block: RmSyncOptions.() -> Unit,
        ): RmSyncOptions = js("{}").unsafeCast<RmSyncOptions>().apply(block)
    }
}
