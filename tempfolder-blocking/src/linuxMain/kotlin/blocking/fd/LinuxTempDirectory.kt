/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.TempfolderException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.blocking.Tempfolder
import at.released.tempfolder.blocking.generateTempDirectoryName
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
import at.released.tempfolder.posix200809.blocking.fd.PosixTempDirectoryCreator
import at.released.tempfolder.posix200809.blocking.fd.PosixTempDirectoryCreator.ResolvedTempRoot
import at.released.tempfolder.posix200809.blocking.fd.PosixTempRootResolver
import at.released.tempfolder.posix200809.blocking.fd.PosixTempfolder
import at.released.tempfolder.posix200809.dsl.AdvisoryLockType
import at.released.tempfolder.posix200809.dsl.AdvisoryLockType.EXCLUSIVE
import at.released.tempfolder.posix200809.dsl.AdvisoryLockType.NONE
import at.released.tempfolder.posix200809.dsl.AdvisoryLockType.SHARED
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.platformUnlinkDirectory
import at.released.tempfolder.posix200809.toPosixMode
import platform.linux.flock
import platform.posix.LOCK_EX
import platform.posix.LOCK_SH
import platform.posix.errno

@Throws(TempfolderException::class)
public fun Tempfolder.Companion.createLinuxTempDirectory(
    block: LinuxTempDirectoryConfig.() -> Unit,
): Tempfolder<TempfolderPosixFileDescriptor> {
    val config = LinuxTempDirectoryConfig().apply(block)
    val root: ResolvedTempRoot = PosixTempRootResolver.resolve(config.base)
    val coordinates = PosixTempDirectoryCreator.createDirectory(
        root = root,
        mode = config.permissions.toPosixMode(),
        nameGenerator = { generateTempDirectoryName(config.prefix) },
    )
    try {
        setLock(coordinates.directoryDescriptor, type = config.advisoryLock)
    } catch (ie: TempfolderIOException) {
        val errno = platformUnlinkDirectory(coordinates.parentDirfd, coordinates.directoryPathname)
        if (errno != 0) {
            ie.addSuppressed(
                TempfolderNativeIOException(errno, "Can not remove temp directory. ${errnoDescription()}"),
            )
        }
        throw ie
    }
    return PosixTempfolder(
        parentDirfd = coordinates.parentDirfd,
        directoryPathname = coordinates.directoryPathname,
        root = coordinates.directoryDescriptor,
    )
}

@Throws(TempfolderIOException::class)
private fun setLock(fd: TempfolderPosixFileDescriptor, type: AdvisoryLockType) {
    val lockOp = when (type) {
        NONE -> return
        EXCLUSIVE -> LOCK_EX
        SHARED -> LOCK_SH
    }
    val resultCode = flock(fd.fd, lockOp)
    if (resultCode == -1) {
        throw TempfolderNativeIOException(errno, "Can not set advisory lock on directory. ${errnoDescription()}")
    }
}
