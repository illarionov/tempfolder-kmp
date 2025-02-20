/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderWasiIOException
import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.TempfolderPathString.MultibytePathString
import at.released.tempfolder.path.WasiPathString.Companion.toWasiPathString
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType
import at.released.tempfolder.testframework.wasip1.wasiCreateFileOrThrow
import at.released.tempfolder.testframework.wasip1.wasiFdWriteOrThrow
import at.released.tempfolder.testframework.wasip1.wasiPathFilestatGet
import at.released.tempfolder.testframework.wasip1.wasiPathSymlinkOrThrow
import at.released.tempfolder.wasip1.WasiPreopens
import at.released.tempfolder.wasip1.allocateString
import at.released.tempfolder.wasip1.type.Errno
import at.released.tempfolder.wasip1.type.Filetype.DIRECTORY
import at.released.tempfolder.wasip1.type.Filetype.SYMBOLIC_LINK
import at.released.tempfolder.wasip1.wasiCloseOrThrow
import at.released.tempfolder.wasip1.wasiCreateDirectoryOrThrow
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.bytestring.isEmpty
import kotlin.wasm.unsafe.withScopedMemoryAllocator

internal actual val platformFilesystem: PlatformFilesystemTestFunctions get() = WasiFilesystemTestFunctions

@Suppress("TooManyFunctions")
private object WasiFilesystemTestFunctions : PlatformFilesystemTestFunctions {
    override val isPosixFileModeSupported: Boolean get() = false
    override val isSymlinkSupported: Boolean get() = true
    override val pathSeparator: Char get() = '/'
    private val wasiPreopens = WasiPreopens.load()

    override fun joinPath(base: TempfolderPathString, append: String): TempfolderPathString {
        check(base is MultibytePathString)
        return base.bytes.toWasiPathString().append(append)
    }

    override fun isDirectory(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        val (fd, relativePath) = wasiPreopens.resolvePath(path)
        return wasiPathFilestatGet(fd, relativePath, followBasenameSymlink).filetype == DIRECTORY
    }

    override fun isFile(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        val (fd, relativePath) = wasiPreopens.resolvePath(path)
        return wasiPathFilestatGet(fd, relativePath, followBasenameSymlink).filetype.let {
            it != DIRECTORY && it != SYMBOLIC_LINK
        }
    }

    override fun isSymlink(path: TempfolderPathString): Boolean {
        val (fd, relativePath) = wasiPreopens.resolvePath(path)
        return wasiPathFilestatGet(fd, relativePath, false).filetype == SYMBOLIC_LINK
    }

    override fun isExists(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        val (fd, relativePath) = wasiPreopens.resolvePath(path)
        return try {
            wasiPathFilestatGet(fd, relativePath, false).filetype
            true
        } catch (ie: TempfolderWasiIOException) {
            if (ie.wasiErrno == Errno.NOENT.code) {
                false
            } else {
                throw ie
            }
        }
    }

    override fun isSamePathAs(path1: TempfolderPathString, path2: TempfolderPathString): Boolean {
        return path1.toString() == path2.toString() // XXX
    }

    override fun getFileMode(path: TempfolderPathString, followBasenameSymlink: Boolean): Set<TempfolderFileModeBit> {
        error("Not supported")
    }

    override fun createFile(path: TempfolderPathString, mode: Set<TempfolderFileModeBit>, content: ByteString) {
        val (fd, relativePath) = wasiPreopens.resolvePath(path)
        val fileFd = wasiCreateFileOrThrow(fd, relativePath, false)
        try {
            writeToFile(fileFd, content)
        } finally {
            wasiCloseOrThrow(fileFd)
        }
    }

    override fun createDirectory(path: TempfolderPathString, mode: Set<TempfolderFileModeBit>) {
        val (fd, relativePath) = wasiPreopens.resolvePath(path)
        wasiCreateDirectoryOrThrow(fd, relativePath)
    }

    override fun createSymlink(oldPath: String, newPath: TempfolderPathString, type: SymlinkType) {
        val (fd, newRelativePath) = wasiPreopens.resolvePath(newPath)
        wasiPathSymlinkOrThrow(oldPath.encodeToByteString(), fd, newRelativePath)
    }

    private fun writeToFile(fd: TempDirectoryDescriptor, content: ByteString) {
        if (content.isEmpty()) {
            return
        }
        withScopedMemoryAllocator { allocator ->
            val (bytes, total) = allocator.allocateString(content)
            var byteLeft: Int = content.size
            while (byteLeft > 0) {
                val written: Int = wasiFdWriteOrThrow(fd, bytes + total - byteLeft, byteLeft)
                if (written >= 0) {
                    byteLeft -= written
                }
            }
        }
    }
}
