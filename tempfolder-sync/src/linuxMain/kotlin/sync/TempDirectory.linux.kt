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
import at.released.tempfolder.sync.fd.LinuxTempDirectoryConfig
import at.released.tempfolder.sync.fd.createLinuxTempDirectoryInternal

internal actual fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*> {
    return TempDirectory.createLinuxTempDirectory {
        setFromCommon(config)
    }
}

/**
 * Creates a temporary directory on the file system with Linux-specific configuration [block].
 *
 * [TempDirectory.root] property represents a native file descriptor encapsulated by [TempDirectoryDescriptor].
 *
 * @throws TempDirectoryIOException on errors during directory creation.
 * @see [createTempDirectory]
 */
@Throws(TempDirectoryException::class)
public fun TempDirectory.Companion.createLinuxTempDirectory(
    block: LinuxTempDirectoryConfig.() -> Unit,
): TempDirectory<TempDirectoryDescriptor> = createLinuxTempDirectoryInternal(block)

private fun LinuxTempDirectoryConfig.setFromCommon(commonConfig: CommonTempDirectoryConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> TempDirectoryPosixBase.Auto {
            sizeEstimate = commonBase.sizeEstimate
        }

        is Path -> TempDirectoryPosixBase.Path(commonBase.path)
    }
    prefix = commonConfig.prefix
    permissions = commonConfig.permissions
}
