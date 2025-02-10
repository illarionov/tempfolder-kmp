/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.toPosixPathString
import at.released.tempfolder.testframework.PlatformFilesystemTestFunctions.SymlinkType
import kotlinx.io.bytestring.ByteString
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE
import java.nio.file.attribute.PosixFilePermission.GROUP_READ
import java.nio.file.attribute.PosixFilePermission.GROUP_WRITE
import java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE
import java.nio.file.attribute.PosixFilePermission.OTHERS_READ
import java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE
import java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import java.nio.file.attribute.PosixFilePermission.OWNER_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_WRITE
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
    val filesystem: FileSystem = FileSystems.getDefault(),
) : PlatformFilesystemTestFunctions {
    override val isPosixFileModeSupported: Boolean = filesystem.supportedFileAttributeViews().contains("posix")
    override val isSymlinkSupported: Boolean = true
    override val pathSeparator: Char = filesystem.separator[0]

    private val TempfolderPathString.nioPath: Path get() = filesystem.getPath(this.asString())

    override fun resolvePath(base: TempfolderPathString, append: String): TempfolderPathString {
        return filesystem.getPath(base.asString(), append).absolutePathString().toPosixPathString()
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
        arrayOf(PosixFilePermissions.asFileAttribute(this.toPosixPermission()))
    } else {
        emptyArray()
    }

    private companion object {
        private val PosixFilePermission.tempfolderBit: TempfolderFileModeBit
            get() = when (this) {
                OWNER_READ -> TempfolderFileModeBit.USER_READ
                OWNER_WRITE -> TempfolderFileModeBit.USER_WRITE
                OWNER_EXECUTE -> TempfolderFileModeBit.USER_EXECUTE
                GROUP_READ -> TempfolderFileModeBit.GROUP_READ
                GROUP_WRITE -> TempfolderFileModeBit.GROUP_WRITE
                GROUP_EXECUTE -> TempfolderFileModeBit.GROUP_EXECUTE
                OTHERS_READ -> TempfolderFileModeBit.OTHER_READ
                OTHERS_WRITE -> TempfolderFileModeBit.OTHER_WRITE
                OTHERS_EXECUTE -> TempfolderFileModeBit.OTHER_EXECUTE
            }

        private fun linkOptions(followSymlinks: Boolean): Array<LinkOption> = if (followSymlinks) {
            emptyArray()
        } else {
            arrayOf(LinkOption.NOFOLLOW_LINKS)
        }

        private fun Set<PosixFilePermission>.toTempfolderPermissions(): Set<TempfolderFileModeBit> {
            return this.mapTo(mutableSetOf()) { it.tempfolderBit }
        }

        private fun Set<TempfolderFileModeBit>.toPosixPermission(): Set<PosixFilePermission> {
            return PosixFilePermission.entries.mapNotNullTo(mutableSetOf()) { posixBit ->
                posixBit.takeIf { it.tempfolderBit in this }
            }
        }
    }
}
