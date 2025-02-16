/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.path

import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.blocking.Tempfolder
import at.released.tempfolder.jsapi.nodejs.RmSyncOptions
import at.released.tempfolder.jsapi.nodejs.join
import at.released.tempfolder.jsapi.nodejs.rmSync
import at.released.tempfolder.path.JsNodePathString.Companion.toJsNodePathString
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.TempfolderPathString
import kotlinx.atomicfu.atomic

public fun Tempfolder.Companion.createNodeJsTempDirectory(
    block: NodeJsTempDirectoryConfig.() -> Unit = {},
): Tempfolder<String> {
    val config = NodeJsTempDirectoryConfig().apply(block)
    val tempRoot: String = NodeJsTempPathResolver.resolve(config.base)
    val tempDirectory = NodeJsTempDirectoryCreator.createDirectory(tempRoot, config.permissions)
    return NodeJsTempDirectory(tempDirectory)
}

private class NodeJsTempDirectory(
    absolutePath: String,
) : Tempfolder<String> {
    override var deleteOnClose: Boolean by atomic(true)
    override val root: String = absolutePath
    private val isClosed = atomic(false)

    override fun getAbsolutePath(): TempfolderPathString = root.toJsNodePathString()

    override fun delete() {
        throwIfClosed()
        deleteDirectoryRecursively(root)
    }

    override fun resolve(name: String): TempfolderPathString {
        try {
            return join(root, name).toJsNodePathString()
        } catch (@Suppress("TooGenericExceptionCaught") err: Exception) {
            throw TempfolderInvalidPathException("join(`$root`, `$name`) failed", err)
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
            throw TempfolderClosedException(TEMPFOLDER_CLOSED_MESSAGE)
        }
    }

    private companion object {
        fun deleteDirectoryRecursively(path: String) {
            try {
                rmSync(path, RmSyncOptions { recursive = true })
            } catch (@Suppress("TooGenericExceptionCaught") err: Exception) {
                throw TempfolderIOException("rmSync() failed", err)
            }
        }

        private inline fun RmSyncOptions(
            block: RmSyncOptions.() -> Unit,
        ): RmSyncOptions = js("{}").unsafeCast<RmSyncOptions>().apply(block)
    }
}
