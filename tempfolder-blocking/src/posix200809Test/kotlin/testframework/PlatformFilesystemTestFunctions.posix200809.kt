/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.dsl.TempfolderFileModeBit.Companion
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.TempfolderPathString.MultibytePathString
import at.released.tempfolder.path.TempfolderPathString.WideCharPathString
import at.released.tempfolder.path.UnknownEncodingPosixPathString
import at.released.tempfolder.path.appendPosixPath
import at.released.tempfolder.path.toPosixPathString
import at.released.tempfolder.posix200809.PosixFileType
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor.Companion.CURRENT_WORKING_DIRECTORY
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
    dirFd: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    followBaseSymlink: Boolean = false,
): UInt

@Suppress("TooManyFunctions")
internal object Posix2009809FilesystemTestFunctions : PlatformFilesystemTestFunctions {
    override val isPosixFileModeSupported: Boolean = true
    override val isSymlinkSupported: Boolean = true
    override val pathSeparator: Char = '/'

    override fun resolvePath(base: TempfolderPathString, append: String): TempfolderPathString {
        check(base is MultibytePathString)
        return UnknownEncodingPosixPathString(base.bytes.appendPosixPath(append))
    }

    override fun isDirectory(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return platformGetFileType(CURRENT_WORKING_DIRECTORY, path.toPosixPathString()) == PosixFileType.DIRECTORY
    }

    override fun isFile(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return platformGetFileType(CURRENT_WORKING_DIRECTORY, path.toPosixPathString()) == PosixFileType.FILE
    }

    override fun isSymlink(path: TempfolderPathString): Boolean {
        return platformGetFileType(CURRENT_WORKING_DIRECTORY, path.toPosixPathString()) == PosixFileType.SYMLINK
    }

    override fun isExists(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return try {
            platformGetFileType(CURRENT_WORKING_DIRECTORY, path.toPosixPathString())
            true
        } catch (nfe: TempfolderNativeIOException) {
            if (nfe.errno != ENOENT) {
                throw nfe
            } else {
                false
            }
        }
    }

    override fun isSamePathAs(path1: TempfolderPathString, path2: TempfolderPathString): Boolean {
        val path1realPath = platformRealpath(path1.toPosixPathString())
        val path2realPath = platformRealpath(path2.toPosixPathString())
        return path1realPath == path2realPath
    }

    override fun getFileMode(path: TempfolderPathString, followBasenameSymlink: Boolean): Set<TempfolderFileModeBit> {
        return nativeGetFileMode(CURRENT_WORKING_DIRECTORY, path.toPosixPathString(), followBasenameSymlink)
            .let(Companion::fromPosixMode)
    }

    override fun createFile(path: TempfolderPathString, mode: Set<TempfolderFileModeBit>, content: ByteString) {
        val fd: TempfolderPosixFileDescriptor = platformOpenAt(
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

    override fun createDirectory(path: TempfolderPathString, mode: Set<TempfolderFileModeBit>) {
        platformMkdirat(CURRENT_WORKING_DIRECTORY, path.toPosixPathString(), mode.toPosixMode())
    }

    override fun createSymlink(oldPath: String, newPath: TempfolderPathString, type: SymlinkType) {
        val result = symlink(oldPath, newPath.asString())
        if (result == -1) {
            throw TempfolderNativeIOException(errno, "symlink() failed. ${errnoDescription()}")
        }
    }

    private fun TempfolderPathString.toPosixPathString(): PosixPathString {
        return when (this) {
            is PosixPathString -> this
            is MultibytePathString -> UnknownEncodingPosixPathString(this.bytes)
            is WideCharPathString -> this.asString().toPosixPathString()
        }
    }

    @OptIn(UnsafeNumber::class)
    @Throws(TempfolderNativeIOException::class)
    private fun writeToFile(fd: TempfolderPosixFileDescriptor, content: ByteString) {
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
                    throw TempfolderNativeIOException(errno, "write() failed. ${errnoDescription()}")
                }
            }
        }
    }
}
