/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import assertk.assertThat
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
import at.released.tempfolder.sync.TempDirectory
import at.released.tempfolder.testframework.assertions.isSymlink
import kotlinx.io.bytestring.ByteString

internal expect val platformFilesystem: PlatformFilesystemTestFunctions

interface PlatformFilesystemTestFunctions {
    val isPosixFileModeSupported: Boolean
    val isSymlinkSupported: Boolean
    val canSymlinkOutsidePreopen: Boolean get() = isSymlinkSupported
    val symlinkTargetOutsidePreopen: String get() = "../../../../build"
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

internal fun TempDirectory<*>.createSymlinkToDirectoryOutsideTree() {
    if (!platformFilesystem.canSymlinkOutsidePreopen) return

    // Put the symlink inside the temporary tree and point it at a build directory outside it. The relative
    // path can reach an existing directory on the host, depending on where the platform creates the tree.
    // POSIX systems and Windows allow this kind of symlink (a process with restricted path resolution may
    // see it as dangling even when the target exists). A missing or inaccessible target is fine for this test.
    // Cleanup should remove the link itself without following it into the build directory.
    val target = platformFilesystem.symlinkTargetOutsidePreopen
    val link = append("build.link")
    platformFilesystem.createSymlink(target, link)
    assertThat(link).isSymlink()
}
