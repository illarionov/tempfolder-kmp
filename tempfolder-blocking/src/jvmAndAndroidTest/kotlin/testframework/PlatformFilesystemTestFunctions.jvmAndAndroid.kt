/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.blocking.linkOptions
import at.released.tempfolder.blocking.toNioPosixPermissions
import at.released.tempfolder.blocking.toTempfolderPermissions
import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.toPosixPathString
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType
import kotlinx.io.bytestring.ByteString
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.createSymbolicLinkPointingTo
import kotlin.io.path.exists
import kotlin.io.path.fileAttributesView
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSameFileAs
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.outputStream

private val nioDefaultInstance = NioFilesystemTestFunctions()

internal actual val platformFilesystem: PlatformFilesystemTestFunctions
    get() = nioDefaultInstance

@Suppress("TooManyFunctions")
internal class NioFilesystemTestFunctions(
    private val fileSystem: FileSystem = FileSystems.getDefault(),
) : PlatformFilesystemTestFunctions {
    override val isPosixFileModeSupported: Boolean = fileSystem.supportedFileAttributeViews().contains("posix")
    override val isSymlinkSupported: Boolean = true
    override val pathSeparator: Char = fileSystem.separator[0]

    private val TempfolderPathString.nioPath: Path get() = fileSystem.getPath(this.asString())

    override fun resolvePath(base: TempfolderPathString, append: String): TempfolderPathString {
        return fileSystem.getPath(base.asString(), append).absolutePathString().toPosixPathString()
    }

    override fun isDirectory(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return path.nioPath.isDirectory(options = linkOptions(followBasenameSymlink))
    }

    override fun isFile(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return path.nioPath.isRegularFile(options = linkOptions(followBasenameSymlink))
    }

    override fun isSymlink(path: TempfolderPathString): Boolean {
        return path.nioPath.isSymbolicLink()
    }

    override fun isExists(path: TempfolderPathString, followBasenameSymlink: Boolean): Boolean {
        return path.nioPath.exists(options = linkOptions(followBasenameSymlink))
    }

    override fun isSamePathAs(path1: TempfolderPathString, path2: TempfolderPathString): Boolean {
        return path1.nioPath.isSameFileAs(path2.nioPath)
    }

    override fun getFileMode(path: TempfolderPathString, followBasenameSymlink: Boolean): Set<TempfolderFileModeBit> {
        return path.nioPath.fileAttributesView<PosixFileAttributeView>().readAttributes().permissions()
            .toTempfolderPermissions()
    }

    override fun createFile(path: TempfolderPathString, mode: Set<TempfolderFileModeBit>, content: ByteString) {
        path.nioPath.createFile(attributes = mode.asFileAttributes()).outputStream().buffered().use {
            it.write(content.toByteArray())
        }
    }

    override fun createDirectory(path: TempfolderPathString, mode: Set<TempfolderFileModeBit>) {
        path.nioPath.createDirectory(attributes = mode.asFileAttributes())
    }

    override fun createSymlink(oldPath: String, newPath: TempfolderPathString, type: SymlinkType) {
        newPath.nioPath.createSymbolicLinkPointingTo(Path.of(oldPath))
    }

    private fun Set<TempfolderFileModeBit>.asFileAttributes() = if (isPosixFileModeSupported) {
        arrayOf(PosixFilePermissions.asFileAttribute(this.toNioPosixPermissions()))
    } else {
        emptyArray()
    }
}
