/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.path

import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.blocking.Tempfolder
import at.released.tempfolder.jsapi.nodejs.join
import at.released.tempfolder.jsapi.nodejs.rmSync
import at.released.tempfolder.path.NodeJsPathString.Companion.toJsNodePathString
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.TempfolderPathString
import kotlinx.atomicfu.atomic

// XXX keep in sync with NodeJsTempDirectory

public fun Tempfolder.Companion.createWasmNodeJsTempDirectory(
    block: NodeJsTempDirectoryConfig.() -> Unit = {},
): Tempfolder<String> {
    val config = NodeJsTempDirectoryConfig().apply(block)
    val tempRoot: String = WasmNodeJsTempPathResolver.resolve(config.base)
    val tempDirectory = WasmNodeJsTempDirectoryCreator.createDirectory(tempRoot, config.permissions)
    return WasmNodeJsTempDirectory(tempDirectory)
}

@Suppress("UnusedParameter")
private fun createRmsyncOptions(recursive: Boolean): JsAny = js("({recursive: recursive})")

private class WasmNodeJsTempDirectory(
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
        } catch (err: JsException) {
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
                rmSync(path, createRmsyncOptions(true))
            } catch (err: JsException) {
                throw TempfolderIOException("rmSync() failed", err)
            }
        }
    }
}
