/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.dsl.CommonTempDirectoryConfig
import at.released.tempfolder.dsl.TempDirectoryBase.Auto
import at.released.tempfolder.dsl.TempDirectoryBase.Path
import at.released.tempfolder.sync.fd.TempDirectoryWasip1TempBase
import at.released.tempfolder.sync.fd.Wasip1TempDirectory
import at.released.tempfolder.sync.fd.Wasip1TempDirectoryConfig

internal actual fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*> {
    return TempDirectory.createWasip1TempDirectory {
        setFromCommon(config)
    }
}

/**
 * Creates a temporary directory on the WASI Preview 1 file system with configuration [block].
 *
 * [TempDirectory.root] property represents a WASI file descriptor encapsulated by [TempDirectoryDescriptor].
 *
 * @throws TempDirectoryIOException on errors during directory creation.
 * @see [createTempDirectory]
 */
public fun TempDirectory.Companion.createWasip1TempDirectory(
    block: Wasip1TempDirectoryConfig.() -> Unit,
): TempDirectory<TempDirectoryDescriptor> = Wasip1TempDirectory.create(block)

private fun Wasip1TempDirectoryConfig.setFromCommon(commonConfig: CommonTempDirectoryConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> TempDirectoryWasip1TempBase.Auto
        is Path -> TempDirectoryWasip1TempBase.Path(commonBase.path)
    }
    prefix = commonConfig.prefix
}
