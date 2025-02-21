/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.dsl.TempDirectoryFileModeBit
import at.released.tempfolder.jsapi.nodejs.fromNodeJsMode
import at.released.tempfolder.jsapi.nodejs.join
import at.released.tempfolder.jsapi.nodejs.mkdirSync
import at.released.tempfolder.jsapi.nodejs.realpathSync
import at.released.tempfolder.jsapi.nodejs.toNodeJsMode
import at.released.tempfolder.path.JsNodePath.Companion.toJsNodePath
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType.NOT_SPECIFIED
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType.SYMLINK_TO_DIRECTORY
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType.SYMLINK_TO_FILE
import at.released.tempfolder.testframework.jsapi.nodejs.Stats
import at.released.tempfolder.testframework.jsapi.nodejs.lstatSync
import at.released.tempfolder.testframework.jsapi.nodejs.statSync
import at.released.tempfolder.testframework.jsapi.nodejs.symlinkSync
import at.released.tempfolder.testframework.jsapi.nodejs.writeFileSync
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString

internal actual val platformFilesystem: PlatformFilesystemTestFunctions
    get() = WasmNodeJsFilesystemTestFunctions

private fun getNodejsPlatform(): String = js("globalThis.process.platform")

@Suppress("UnusedParameter")
private fun createWriteFileSynOptions(mode: Int): JsAny = js("({mode: mode})")

@Suppress("UnusedParameter")
private fun createLstatOptions(throwIfNoEntry: Boolean): JsAny =
    js("({throwIfNoEntry: throwIfNoEntry, bigint: true})")

@Suppress("TooManyFunctions")
private object WasmNodeJsFilesystemTestFunctions : PlatformFilesystemTestFunctions {
    override val isPosixFileModeSupported: Boolean get() = getNodejsPlatform() != "win32"
    override val isSymlinkSupported: Boolean get() = true
    override val pathSeparator: Char get() = '/'

    override fun joinPath(base: TempDirectoryPath, append: String): TempDirectoryPath {
        return join(base.asString(), append).toJsNodePath()
    }

    override fun isDirectory(path: TempDirectoryPath, followBasenameSymlink: Boolean): Boolean {
        return getStatOrThrow(path, followBasenameSymlink)?.isDirectory() ?: false
    }

    override fun isFile(path: TempDirectoryPath, followBasenameSymlink: Boolean): Boolean {
        return getStatOrThrow(path, followBasenameSymlink)?.let {
            return !it.isDirectory() && !it.isSymbolicLink()
        } ?: false
    }

    override fun isSymlink(path: TempDirectoryPath): Boolean {
        return getStatOrThrow(path, false)?.isSymbolicLink() ?: false
    }

    override fun isExists(path: TempDirectoryPath, followBasenameSymlink: Boolean): Boolean {
        return getStatOrThrow(path, followBasenameSymlink) != null
    }

    override fun isSamePathAs(path1: TempDirectoryPath, path2: TempDirectoryPath): Boolean {
        val realPath1 = realpathSync(path1.asString())
        val realPath2 = realpathSync(path2.asString())
        return realPath1 == realPath2
    }

    override fun getFileMode(path: TempDirectoryPath, followBasenameSymlink: Boolean): Set<TempDirectoryFileModeBit> {
        val mode = getStatOrThrow(path, followBasenameSymlink)?.mode?.toInt()
            ?: throw TempDirectoryIOException("File `$path` not found")
        return TempDirectoryFileModeBit.fromNodeJsMode(mode)
    }

    override fun createFile(path: TempDirectoryPath, mode: Set<TempDirectoryFileModeBit>, content: ByteString) {
        try {
            writeFileSync(
                file = path.asString(),
                data = content.toHexString(), // XXX find how to write bytes
                opts = createWriteFileSynOptions(mode = mode.toNodeJsMode()),
            )
        } catch (err: JsException) {
            throw TempDirectoryIOException("writeFileSync() failed. Message: `${err.message}`", err)
        }
    }

    override fun createDirectory(path: TempDirectoryPath, mode: Set<TempDirectoryFileModeBit>) {
        val intMode = mode.toNodeJsMode()
        try {
            mkdirSync(path.asString(), intMode)
        } catch (err: JsException) {
            throw TempDirectoryIOException("mkdirSync() failed. `${err.message}`", err)
        }
    }

    override fun createSymlink(oldPath: String, newPath: TempDirectoryPath, type: SymlinkType) {
        val typeString = when (type) {
            NOT_SPECIFIED -> null
            SYMLINK_TO_FILE -> "file"
            SYMLINK_TO_DIRECTORY -> "dir"
        }
        try {
            symlinkSync(oldPath, newPath.asString(), typeString)
        } catch (err: JsException) {
            throw TempDirectoryIOException("symlinkSync() failed. `${err.message}`", err)
        }
    }

    private fun getStatOrThrow(path: TempDirectoryPath, followBasenameSymlink: Boolean): Stats? {
        try {
            val lstatOptions = createLstatOptions(false)
            return if (followBasenameSymlink) {
                statSync(path.asString(), lstatOptions)
            } else {
                lstatSync(path.asString(), lstatOptions)
            }
        } catch (err: JsException) {
            throw TempDirectoryIOException("statSync() failed. `${err.message}`", err)
        }
    }
}
