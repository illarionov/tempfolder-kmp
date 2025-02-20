/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.dsl.CommonTempDirectoryConfig
import at.released.tempfolder.dsl.TempDirectoryBase.Auto
import at.released.tempfolder.dsl.TempDirectoryBase.Path
import at.released.tempfolder.sync.path.NodeTempDirectoryConfig
import at.released.tempfolder.sync.path.TempDirectoryNodeBase
import at.released.tempfolder.sync.path.createNodeJsTempDirectory

internal actual fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*> {
    return TempDirectory.createNodeJsTempDirectory {
        setFromCommon(config)
    }
}

private fun NodeTempDirectoryConfig.setFromCommon(commonConfig: CommonTempDirectoryConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> TempDirectoryNodeBase.Auto
        is Path -> TempDirectoryNodeBase.Path(commonBase.path)
    }
    prefix = commonConfig.prefix
    permissions = commonConfig.permissions
}
