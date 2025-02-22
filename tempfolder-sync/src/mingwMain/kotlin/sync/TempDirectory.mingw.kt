/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.dsl.CommonTempDirectoryConfig
import at.released.tempfolder.dsl.TempDirectoryBase.Auto
import at.released.tempfolder.dsl.TempDirectoryBase.Path
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.WindowsPath
import at.released.tempfolder.sync.path.WindowsTempDirectory
import at.released.tempfolder.sync.path.WindowsTempDirectoryConfig
import at.released.tempfolder.sync.path.WindowsTempDirectoryConfig.Companion.auto
import at.released.tempfolder.sync.path.WindowsTempDirectoryConfig.Companion.path
import at.released.tempfolder.sync.path.WindowsTempDirectoryCreator
import at.released.tempfolder.sync.path.WindowsTempPathResolver

internal actual fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*> {
    return TempDirectory.createWindowsTempDirectory {
        setFromCommon(config)
    }
}

/**
 * Creates a temporary directory on the file system with Windows-specific configuration [block].
 *
 * This implementation is based on the use of the Windows API functions.
 *
 * [TempDirectory.root] property represents a real path on Windows file system.
 *
 * @throws TempDirectoryIOException on errors during directory creation.
 * @see [createTempDirectory]
 */
@Throws(TempDirectoryException::class)
public fun TempDirectory.Companion.createWindowsTempDirectory(
    block: WindowsTempDirectoryConfig.() -> Unit = {},
): TempDirectory<TempDirectoryPath> {
    val config = WindowsTempDirectoryConfig().apply(block)
    val tempRoot: WindowsPath = WindowsTempPathResolver.resolve(config.base)
    val tempDirectory = WindowsTempDirectoryCreator.createDirectory(tempRoot)
    return WindowsTempDirectory(tempDirectory)
}

private fun WindowsTempDirectoryConfig.setFromCommon(commonConfig: CommonTempDirectoryConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> auto()
        is Path -> path(commonBase.path)
    }
    prefix = commonConfig.prefix
}
