/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.dsl.CommonTempDirectoryConfig
import at.released.tempfolder.dsl.TempDirectoryBase.Auto
import at.released.tempfolder.dsl.TempDirectoryBase.Path
import at.released.tempfolder.posix200809.dsl.TempDirectoryPosixBase
import at.released.tempfolder.posix200809.sync.fd.PosixTempDirectory
import at.released.tempfolder.posix200809.sync.fd.PosixTempDirectoryCreator
import at.released.tempfolder.posix200809.sync.fd.PosixTempRootResolver
import at.released.tempfolder.posix200809.sync.fd.PosixTempRootResolver.ResolvedTempRoot
import at.released.tempfolder.posix200809.toPosixMode
import at.released.tempfolder.sync.fd.AppleTempDirectoryConfig
import at.released.tempfolder.sync.nsfm.NsurlTempDirectory
import at.released.tempfolder.sync.nsfm.NsurlTempDirectoryConfig
import at.released.tempfolder.sync.nsfm.createAppleNsurlTempDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

internal actual fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*> {
    return TempDirectory.createAppleTempDirectory {
        setFromCommon(config)
    }
}

/**
 * Creates a temporary directory on the file system with Apple-specific configuration [block].
 *
 * This implementation is based on the use of POSIX functions for working with file systems.
 *
 * [TempDirectory.root] property represents a native file descriptor encapsulated by [TempDirectoryDescriptor].
 *
 * @throws TempDirectoryIOException on errors during directory creation.
 * @see [createTempDirectory]
 * @see [createAppleNsurlTempDirectory]
 */
@Throws(TempDirectoryException::class)
public fun TempDirectory.Companion.createAppleTempDirectory(
    block: AppleTempDirectoryConfig.() -> Unit = {},
): TempDirectory<TempDirectoryDescriptor> {
    val config = AppleTempDirectoryConfig().apply(block)
    val root: ResolvedTempRoot = PosixTempRootResolver.resolve(config.base)
    val coordinates = PosixTempDirectoryCreator.createDirectory(
        root = root,
        mode = config.permissions.toPosixMode(),
        nameGenerator = { generateTempDirectoryName(config.prefix) },
    )
    return PosixTempDirectory(
        parentDirfd = coordinates.parentDirfd,
        directoryPathname = coordinates.directoryPathname,
        root = coordinates.directoryDescriptor,
    )
}

/**
 * Creates a temporary directory on the file system with Apple-specific [NsurlTempDirectoryConfig]
 * configuration [block].
 *
 * This implementation utilizes [NSFileManager].
 *
 * [TempDirectory.root] property represents a [NSURL] URL of the temporary directory.
 *
 * @throws TempDirectoryIOException on errors during directory creation.
 * @see [createTempDirectory]
 * @see [createAppleTempDirectory]
 */
@Throws(TempDirectoryIOException::class)
public fun TempDirectory.Companion.createNsurlTempDirectory(
    block: NsurlTempDirectoryConfig.() -> Unit = {},
): TempDirectory<NSURL> = NsurlTempDirectory.create(block)

private fun AppleTempDirectoryConfig.setFromCommon(commonConfig: CommonTempDirectoryConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> TempDirectoryPosixBase.Auto {
            sizeEstimate = commonBase.sizeEstimate
        }

        is Path -> TempDirectoryPosixBase.Path(commonBase.path)
    }
    prefix = commonConfig.prefix
    permissions = commonConfig.permissions
}
