/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.dsl.TempDirectoryFileModeBit
import at.released.tempfolder.dsl.TempDirectoryFileModeBit.GROUP_EXECUTE
import at.released.tempfolder.dsl.TempDirectoryFileModeBit.GROUP_READ
import at.released.tempfolder.dsl.TempDirectoryFileModeBit.OTHER_EXECUTE
import at.released.tempfolder.dsl.TempDirectoryFileModeBit.OTHER_READ
import at.released.tempfolder.dsl.TempDirectoryFileModeBit.USER_EXECUTE
import at.released.tempfolder.dsl.TempDirectoryFileModeBit.USER_READ
import at.released.tempfolder.dsl.TempDirectoryFileModeBit.USER_WRITE
import at.released.tempfolder.path.TempDirectoryPath
import kotlinx.io.bytestring.ByteString

internal expect val platformFilesystem: PlatformFilesystemTestFunctions

interface PlatformFilesystemTestFunctions {
    val isPosixFileModeSupported: Boolean
    val isSymlinkSupported: Boolean
    val pathSeparator: Char

    @Throws(TempDirectoryException::class)
    fun joinPath(base: TempDirectoryPath, append: String): TempDirectoryPath

    @Throws(TempDirectoryException::class)
    fun isDirectory(path: TempDirectoryPath, followBasenameSymlink: Boolean = false): Boolean

    @Throws(TempDirectoryException::class)
    fun isFile(path: TempDirectoryPath, followBasenameSymlink: Boolean = false): Boolean

    @Throws(TempDirectoryException::class)
    fun isSymlink(path: TempDirectoryPath): Boolean

    @Throws(TempDirectoryException::class)
    fun isExists(path: TempDirectoryPath, followBasenameSymlink: Boolean = false): Boolean

    @Throws(TempDirectoryException::class)
    fun isSamePathAs(path1: TempDirectoryPath, path2: TempDirectoryPath): Boolean

    @Throws(TempDirectoryException::class)
    fun getFileMode(path: TempDirectoryPath, followBasenameSymlink: Boolean = false): Set<TempDirectoryFileModeBit>

    @Throws(TempDirectoryException::class)
    fun createFile(
        path: TempDirectoryPath,
        mode: Set<TempDirectoryFileModeBit> = setOf(USER_READ, USER_WRITE, GROUP_READ, OTHER_READ),
        content: ByteString = ByteString(),
    )

    fun createDirectory(
        path: TempDirectoryPath,
        mode: Set<TempDirectoryFileModeBit> = setOf(
            USER_READ,
            USER_WRITE,
            USER_EXECUTE,
            GROUP_READ,
            GROUP_EXECUTE,
            OTHER_READ,
            OTHER_EXECUTE,
        ),
    )

    fun createSymlink(oldPath: String, newPath: TempDirectoryPath, type: SymlinkType = SymlinkType.NOT_SPECIFIED)

    enum class SymlinkType {
        NOT_SPECIFIED,
        SYMLINK_TO_FILE,
        SYMLINK_TO_DIRECTORY,
    }
}
