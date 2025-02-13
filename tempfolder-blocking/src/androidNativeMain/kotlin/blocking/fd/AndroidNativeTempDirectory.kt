/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.TempfolderException
import at.released.tempfolder.blocking.Tempfolder
import at.released.tempfolder.blocking.generateTempDirectoryName
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
import at.released.tempfolder.posix200809.blocking.fd.PosixTempDirectoryCreator
import at.released.tempfolder.posix200809.blocking.fd.PosixTempDirectoryCreator.ResolvedTempRoot
import at.released.tempfolder.posix200809.blocking.fd.PosixTempRootResolver
import at.released.tempfolder.posix200809.blocking.fd.PosixTempfolder
import at.released.tempfolder.posix200809.toPosixMode

@Throws(TempfolderException::class)
public fun Tempfolder.Companion.createAndroidNativeTempDirectory(
    block: AndroidNativeTempDirectoryConfig.() -> Unit = {},
): Tempfolder<TempfolderPosixFileDescriptor> {
    val config = AndroidNativeTempDirectoryConfig().apply(block)
    val root: ResolvedTempRoot = PosixTempRootResolver.resolve(config.base)
    val coordinates = PosixTempDirectoryCreator.createDirectory(
        root = root,
        mode = config.permissions.toPosixMode(),
        nameGenerator = { generateTempDirectoryName(config.prefix) },
    )
    return PosixTempfolder(
        parentDirfd = coordinates.parentDirfd,
        directoryPathname = coordinates.directoryPathname,
        root = coordinates.directoryDescriptor,
    )
}
