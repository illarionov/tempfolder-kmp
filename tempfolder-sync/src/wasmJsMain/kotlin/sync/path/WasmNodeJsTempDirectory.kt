/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.TempDirectoryClosedException
import at.released.tempfolder.TempDirectoryClosedException.Companion.TEMP_DIRECTORY_CLOSED_MESSAGE
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.jsapi.nodejs.join
import at.released.tempfolder.jsapi.nodejs.rmSync
import at.released.tempfolder.path.JsNodePath.Companion.toJsNodePath
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.sync.TempDirectory
import kotlin.concurrent.atomics.AtomicBoolean

@Suppress("UnusedParameter")
private fun createRmsyncOptions(recursive: Boolean): JsAny = js("({recursive: recursive})")

internal class WasmNodeJsTempDirectory(
    absolutePath: String,
) : TempDirectory<String> {
    private val deleteOnCloseFlag = AtomicBoolean(true)
    override var deleteOnClose: Boolean
        get() = deleteOnCloseFlag.load()
        set(value) = deleteOnCloseFlag.store(value)
    override val root: String = absolutePath
    private val isClosed = AtomicBoolean(false)

    override fun absolutePath(): TempDirectoryPath = root.toJsNodePath()

    override fun delete() {
        throwIfClosed()
        deleteDirectoryRecursively(root)
    }

    override fun append(name: String): TempDirectoryPath {
        try {
            return join(root, name).toJsNodePath()
        } catch (err: JsException) {
            throw TempDirectoryInvalidPathException("join(`$root`, `$name`) failed", err)
        }
    }

    override fun close() {
        if (isClosed.exchange(true)) {
            return
        }
        if (deleteOnClose) {
            deleteDirectoryRecursively(root)
        }
    }

    private fun throwIfClosed() {
        if (isClosed.load()) {
            throw TempDirectoryClosedException(TEMP_DIRECTORY_CLOSED_MESSAGE)
        }
    }

    private companion object {
        fun deleteDirectoryRecursively(path: String) {
            try {
                rmSync(path, createRmsyncOptions(true))
            } catch (err: JsException) {
                throw TempDirectoryIOException("rmSync() failed", err)
            }
        }
    }
}
