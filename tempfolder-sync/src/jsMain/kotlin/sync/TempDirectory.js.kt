/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.dsl.CommonTempDirectoryConfig
import at.released.tempfolder.dsl.TempDirectoryBase.Auto
import at.released.tempfolder.dsl.TempDirectoryBase.Path
import at.released.tempfolder.sync.path.NodeJsTempDirectory
import at.released.tempfolder.sync.path.NodeJsTempDirectoryCreator
import at.released.tempfolder.sync.path.NodeJsTempPathResolver
import at.released.tempfolder.sync.path.NodeTempDirectoryConfig
import at.released.tempfolder.sync.path.TempDirectoryNodeBase

internal actual fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*> {
    return TempDirectory.createNodeJsTempDirectory {
        setFromCommon(config)
    }
}

/**
 * Creates a temporary directory on the file system with NodeJS-specific configuration [block].
 *
 * This implementation is based on the use of synchronous [NodeJS FileSystem](https://nodejs.org/api/fs.html)
 * functions from `node:fs` module.
 *
 * [TempDirectory.root] property represents a real path of the temp directory.
 *
 * @throws TempDirectoryIOException on errors during directory creation.
 * @see [createTempDirectory]
 */
public fun TempDirectory.Companion.createNodeJsTempDirectory(
    block: NodeTempDirectoryConfig.() -> Unit = {},
): TempDirectory<String> {
    val config = NodeTempDirectoryConfig().apply(block)
    val tempRoot: String = NodeJsTempPathResolver.resolve(config.base)
    val tempDirectory = NodeJsTempDirectoryCreator.createDirectory(tempRoot, config.permissions)
    return NodeJsTempDirectory(tempDirectory)
}

private fun NodeTempDirectoryConfig.setFromCommon(commonConfig: CommonTempDirectoryConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> TempDirectoryNodeBase.Auto
        is Path -> TempDirectoryNodeBase.Path(commonBase.path)
    }
    prefix = commonConfig.prefix
    permissions = commonConfig.permissions
}
