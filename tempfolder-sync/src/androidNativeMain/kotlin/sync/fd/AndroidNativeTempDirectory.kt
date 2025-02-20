/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.fd

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderException
import at.released.tempfolder.posix200809.sync.fd.PosixTempDirectoryCreator
import at.released.tempfolder.posix200809.sync.fd.PosixTempRootResolver
import at.released.tempfolder.posix200809.sync.fd.PosixTempRootResolver.ResolvedTempRoot
import at.released.tempfolder.posix200809.sync.fd.PosixTempfolder
import at.released.tempfolder.posix200809.toPosixMode
import at.released.tempfolder.sync.Tempfolder
import at.released.tempfolder.sync.generateTempDirectoryName

@Throws(TempfolderException::class)
public fun Tempfolder.Companion.createAndroidNativeTempDirectory(
    block: AndroidNativeTempDirectoryConfig.() -> Unit = {},
): Tempfolder<TempDirectoryDescriptor> {
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
