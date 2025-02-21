/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.fd

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.posix200809.TempDirectoryNativeIOException
import at.released.tempfolder.posix200809.dsl.TempDirectoryAdvisoryLockType
import at.released.tempfolder.posix200809.dsl.TempDirectoryAdvisoryLockType.EXCLUSIVE
import at.released.tempfolder.posix200809.dsl.TempDirectoryAdvisoryLockType.NONE
import at.released.tempfolder.posix200809.dsl.TempDirectoryAdvisoryLockType.SHARED
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.platformUnlinkDirectory
import at.released.tempfolder.posix200809.sync.fd.PosixTempDirectory
import at.released.tempfolder.posix200809.sync.fd.PosixTempDirectoryCreator
import at.released.tempfolder.posix200809.sync.fd.PosixTempRootResolver
import at.released.tempfolder.posix200809.sync.fd.PosixTempRootResolver.ResolvedTempRoot
import at.released.tempfolder.posix200809.toPosixMode
import at.released.tempfolder.sync.TempDirectory
import at.released.tempfolder.sync.generateTempDirectoryName
import platform.linux.flock
import platform.posix.LOCK_EX
import platform.posix.LOCK_SH
import platform.posix.errno

@Throws(TempDirectoryException::class)
internal fun createLinuxTempDirectoryInternal(
    block: LinuxTempDirectoryConfig.() -> Unit,
): TempDirectory<TempDirectoryDescriptor> {
    val config = LinuxTempDirectoryConfig().apply(block)
    val root: ResolvedTempRoot = PosixTempRootResolver.resolve(config.base)
    val coordinates = PosixTempDirectoryCreator.createDirectory(
        root = root,
        mode = config.permissions.toPosixMode(),
        nameGenerator = { generateTempDirectoryName(config.prefix) },
    )
    try {
        setLock(coordinates.directoryDescriptor, type = config.advisoryLock)
    } catch (ie: TempDirectoryIOException) {
        val errno = platformUnlinkDirectory(coordinates.parentDirfd, coordinates.directoryPathname)
        if (errno != 0) {
            ie.addSuppressed(
                TempDirectoryNativeIOException(errno, "Can not remove temp directory. ${errnoDescription()}"),
            )
        }
        throw ie
    }
    return PosixTempDirectory(
        parentDirfd = coordinates.parentDirfd,
        directoryPathname = coordinates.directoryPathname,
        root = coordinates.directoryDescriptor,
    )
}

@Throws(TempDirectoryIOException::class)
private fun setLock(fd: TempDirectoryDescriptor, type: TempDirectoryAdvisoryLockType) {
    val lockOp = when (type) {
        NONE -> return
        EXCLUSIVE -> LOCK_EX
        SHARED -> LOCK_SH
    }
    val resultCode = flock(fd.fd, lockOp)
    if (resultCode == -1) {
        throw TempDirectoryNativeIOException(errno, "Can not set advisory lock on directory. ${errnoDescription()}")
    }
}
