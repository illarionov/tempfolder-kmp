/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.jsapi.nodejs.fromNodeJsMode
import at.released.tempfolder.jsapi.nodejs.join
import at.released.tempfolder.jsapi.nodejs.mkdirSync
import at.released.tempfolder.jsapi.nodejs.realpathSync
import at.released.tempfolder.jsapi.nodejs.toNodeJsMode
import at.released.tempfolder.path.NodeJsPathString.Companion.toJsNodePathString
import at.released.tempfolder.path.TempfolderPathString
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

    override fun joinPath(base: TempfolderPathString, append: String): TempfolderPathString {
        return join(base.asString(), append).toJsNodePathString()
    }

    override fun isDirectory(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return getStatOrThrow(path, followBasenameSymlink)?.isDirectory() ?: false
    }

    override fun isFile(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return getStatOrThrow(path, followBasenameSymlink)?.let {
            return !it.isDirectory() && !it.isSymbolicLink()
        } ?: false
    }

    override fun isSymlink(path: TempfolderPathString): Boolean {
        return getStatOrThrow(path, false)?.isSymbolicLink() ?: false
    }

    override fun isExists(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return getStatOrThrow(path, followBasenameSymlink) != null
    }

    override fun isSamePathAs(path1: TempfolderPathString, path2: TempfolderPathString): Boolean {
        val realPath1 = realpathSync(path1.asString())
        val realPath2 = realpathSync(path2.asString())
        return realPath1 == realPath2
    }

    override fun getFileMode(path: TempfolderPathString, followBasenameSymlink: Boolean): Set<TempfolderFileModeBit> {
        val mode = getStatOrThrow(path, followBasenameSymlink)?.mode?.toInt()
            ?: throw TempfolderIOException("File `$path` not found")
        return TempfolderFileModeBit.fromNodeJsMode(mode)
    }

    override fun createFile(path: TempfolderPathString, mode: Set<TempfolderFileModeBit>, content: ByteString) {
        try {
            writeFileSync(
                file = path.asString(),
                data = content.toHexString(), // XXX find how to write bytes
                opts = createWriteFileSynOptions(mode = mode.toNodeJsMode()),
            )
        } catch (err: JsException) {
            throw TempfolderIOException("writeFileSync() failed. Message: `${err.message}`", err)
        }
    }

    override fun createDirectory(path: TempfolderPathString, mode: Set<TempfolderFileModeBit>) {
        val intMode = mode.toNodeJsMode()
        try {
            mkdirSync(path.asString(), intMode)
        } catch (err: JsException) {
            throw TempfolderIOException("mkdirSync() failed. `${err.message}`", err)
        }
    }

    override fun createSymlink(oldPath: String, newPath: TempfolderPathString, type: SymlinkType) {
        val typeString = when (type) {
            NOT_SPECIFIED -> null
            SYMLINK_TO_FILE -> "file"
            SYMLINK_TO_DIRECTORY -> "dir"
        }
        try {
            symlinkSync(oldPath, newPath.asString(), typeString)
        } catch (err: JsException) {
            throw TempfolderIOException("symlinkSync() failed. `${err.message}`", err)
        }
    }

    private fun getStatOrThrow(path: TempfolderPathString, followBasenameSymlink: Boolean): Stats? {
        try {
            val lstatOptions = createLstatOptions(false)
            return if (followBasenameSymlink) {
                statSync(path.asString(), lstatOptions)
            } else {
                lstatSync(path.asString(), lstatOptions)
            }
        } catch (err: JsException) {
            throw TempfolderIOException("statSync() failed. `${err.message}`", err)
        }
    }
}
