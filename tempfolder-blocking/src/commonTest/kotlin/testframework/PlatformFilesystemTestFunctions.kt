/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.TempfolderException
import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.dsl.TempfolderFileModeBit.GROUP_EXECUTE
import at.released.tempfolder.dsl.TempfolderFileModeBit.GROUP_READ
import at.released.tempfolder.dsl.TempfolderFileModeBit.OTHER_EXECUTE
import at.released.tempfolder.dsl.TempfolderFileModeBit.OTHER_READ
import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_EXECUTE
import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_READ
import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_WRITE
import at.released.tempfolder.path.TempfolderPathString
import kotlinx.io.bytestring.ByteString

internal expect val platformFilesystem: PlatformFilesystemTestFunctions

interface PlatformFilesystemTestFunctions {
    val isPosixFileModeSupported: Boolean
    val isSymlinkSupported: Boolean
    val pathSeparator: Char

    @Throws(TempfolderException::class)
    fun resolvePath(base: TempfolderPathString, append: String): TempfolderPathString

    @Throws(TempfolderException::class)
    fun isDirectory(path: TempfolderPathString, followBasenameSymlink: Boolean = false): Boolean

    @Throws(TempfolderException::class)
    fun isFile(path: TempfolderPathString, followBasenameSymlink: Boolean = false): Boolean

    @Throws(TempfolderException::class)
    fun isSymlink(path: TempfolderPathString): Boolean

    @Throws(TempfolderException::class)
    fun isExists(path: TempfolderPathString, followBasenameSymlink: Boolean = false): Boolean

    @Throws(TempfolderException::class)
    fun isSamePathAs(path1: TempfolderPathString, path2: TempfolderPathString): Boolean

    @Throws(TempfolderException::class)
    fun getFileMode(path: TempfolderPathString, followBasenameSymlink: Boolean = false): Set<TempfolderFileModeBit>

    @Throws(TempfolderException::class)
    fun createFile(
        path: TempfolderPathString,
        mode: Set<TempfolderFileModeBit> = setOf(USER_READ, USER_WRITE, GROUP_READ, OTHER_READ),
        content: ByteString = ByteString(),
    )

    fun createDirectory(
        path: TempfolderPathString,
        mode: Set<TempfolderFileModeBit> = setOf(
            USER_READ,
            USER_WRITE,
            USER_EXECUTE,
            GROUP_READ,
            GROUP_EXECUTE,
            OTHER_READ,
            OTHER_EXECUTE,
        ),
    )

    fun createSymlink(oldPath: String, newPath: TempfolderPathString, type: SymlinkType = SymlinkType.NOT_SPECIFIED)

    enum class SymlinkType {
        NOT_SPECIFIED,
        SYMLINK_TO_FILE,
        SYMLINK_TO_DIRECTORY,
    }
}
