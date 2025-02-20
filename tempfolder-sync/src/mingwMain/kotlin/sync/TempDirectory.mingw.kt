/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.dsl.CommonTempDirectoryConfig
import at.released.tempfolder.dsl.TempDirectoryBase.Auto
import at.released.tempfolder.dsl.TempDirectoryBase.Path
import at.released.tempfolder.sync.path.WindowsTempDirectoryConfig
import at.released.tempfolder.sync.path.WindowsTempDirectoryConfig.Companion.auto
import at.released.tempfolder.sync.path.WindowsTempDirectoryConfig.Companion.path
import at.released.tempfolder.sync.path.createWindowsTempDirectory

internal actual fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*> {
    return TempDirectory.createWindowsTempDirectory {
        setFromCommon(config)
    }
}

private fun WindowsTempDirectoryConfig.setFromCommon(commonConfig: CommonTempDirectoryConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> auto()
        is Path -> path(commonBase.path)
    }
    prefix = commonConfig.prefix
    permissions = commonConfig.permissions
}
