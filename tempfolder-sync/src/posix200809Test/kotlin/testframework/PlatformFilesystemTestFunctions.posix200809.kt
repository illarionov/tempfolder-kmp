/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryDescriptor.Companion.CURRENT_WORKING_DIRECTORY
import at.released.tempfolder.dsl.TempDirectoryFileModeBit
import at.released.tempfolder.dsl.TempDirectoryFileModeBit.Companion
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.TempDirectoryPath.MultibytePath
import at.released.tempfolder.path.TempDirectoryPath.WideCharPath
import at.released.tempfolder.path.UnknownEncodingPosixPath
import at.released.tempfolder.path.appendPosixPath
import at.released.tempfolder.path.toPosixPath
import at.released.tempfolder.posix200809.PosixFileType
import at.released.tempfolder.posix200809.TempDirectoryNativeIOException
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.fromPosixMode
import at.released.tempfolder.posix200809.platformGetFileType
import at.released.tempfolder.posix200809.platformMkdirat
import at.released.tempfolder.posix200809.platformOpenAt
import at.released.tempfolder.posix200809.platformRealpath
import at.released.tempfolder.posix200809.toPosixMode
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.isEmpty
import platform.posix.EINTR
import platform.posix.ENOENT
import platform.posix.O_APPEND
import platform.posix.O_CREAT
import platform.posix.O_WRONLY
import platform.posix.close
import platform.posix.errno
import platform.posix.symlink
import platform.posix.write

internal actual val platformFilesystem: PlatformFilesystemTestFunctions = Posix2009809FilesystemTestFunctions

internal expect fun nativeGetFileMode(
    dirFd: TempDirectoryDescriptor,
    path: PosixPath,
    followBaseSymlink: Boolean = false,
): UInt

@Suppress("TooManyFunctions")
internal object Posix2009809FilesystemTestFunctions : PlatformFilesystemTestFunctions {
    override val isPosixFileModeSupported: Boolean = true
    override val isSymlinkSupported: Boolean = true
    override val pathSeparator: Char = '/'

    override fun joinPath(base: TempDirectoryPath, append: String): TempDirectoryPath {
        check(base is MultibytePath)
        return UnknownEncodingPosixPath(base.bytes.appendPosixPath(append))
    }

    override fun isDirectory(path: TempDirectoryPath, followBasenameSymlink: Boolean): Boolean {
        return platformGetFileType(CURRENT_WORKING_DIRECTORY, path.toPosixPathString()) == PosixFileType.DIRECTORY
    }

    override fun isFile(path: TempDirectoryPath, followBasenameSymlink: Boolean): Boolean {
        return platformGetFileType(CURRENT_WORKING_DIRECTORY, path.toPosixPathString()) == PosixFileType.FILE
    }

    override fun isSymlink(path: TempDirectoryPath): Boolean {
        return platformGetFileType(CURRENT_WORKING_DIRECTORY, path.toPosixPathString()) == PosixFileType.SYMLINK
    }

    override fun isExists(path: TempDirectoryPath, followBasenameSymlink: Boolean): Boolean {
        return try {
            platformGetFileType(CURRENT_WORKING_DIRECTORY, path.toPosixPathString())
            true
        } catch (nfe: TempDirectoryNativeIOException) {
            if (nfe.errno != ENOENT) {
                throw nfe
            } else {
                false
            }
        }
    }

    override fun isSamePathAs(path1: TempDirectoryPath, path2: TempDirectoryPath): Boolean {
        val path1realPath = platformRealpath(path1.toPosixPathString())
        val path2realPath = platformRealpath(path2.toPosixPathString())
        return path1realPath == path2realPath
    }

    override fun getFileMode(path: TempDirectoryPath, followBasenameSymlink: Boolean): Set<TempDirectoryFileModeBit> {
        return nativeGetFileMode(CURRENT_WORKING_DIRECTORY, path.toPosixPathString(), followBasenameSymlink)
            .let(Companion::fromPosixMode)
    }

    override fun createFile(path: TempDirectoryPath, mode: Set<TempDirectoryFileModeBit>, content: ByteString) {
        val fd: TempDirectoryDescriptor = platformOpenAt(
            CURRENT_WORKING_DIRECTORY,
            path.toPosixPathString(),
            mode.toPosixMode(),
            (O_APPEND or O_CREAT or O_WRONLY).toULong(),
            resolveBeneath = false,
        )
        try {
            writeToFile(fd, content)
        } finally {
            close(fd.fd) // ignore errors
        }
    }

    override fun createDirectory(path: TempDirectoryPath, mode: Set<TempDirectoryFileModeBit>) {
        platformMkdirat(CURRENT_WORKING_DIRECTORY, path.toPosixPathString(), mode.toPosixMode())
    }

    override fun createSymlink(oldPath: String, newPath: TempDirectoryPath, type: SymlinkType) {
        val result = symlink(oldPath, newPath.asString())
        if (result == -1) {
            throw TempDirectoryNativeIOException(errno, "symlink() failed. ${errnoDescription()}")
        }
    }

    private fun TempDirectoryPath.toPosixPathString(): PosixPath {
        return when (this) {
            is PosixPath -> this
            is MultibytePath -> UnknownEncodingPosixPath(this.bytes)
            is WideCharPath -> this.asString().toPosixPath()
        }
    }

    @OptIn(UnsafeNumber::class)
    @Throws(TempDirectoryNativeIOException::class)
    private fun writeToFile(fd: TempDirectoryDescriptor, content: ByteString) {
        if (content.isEmpty()) {
            return
        }
        content.toByteArray().usePinned { bytesPinned ->
            val total = content.size
            var byteLeft: Int = content.size
            while (byteLeft > 0) {
                val written: Int = write(fd.fd, bytesPinned.addressOf(total - byteLeft), byteLeft.convert()).toInt()
                if (written >= 0) {
                    byteLeft -= written
                } else if (errno != EINTR) {
                    throw TempDirectoryNativeIOException(errno, "write() failed. ${errnoDescription()}")
                }
            }
        }
    }
}
