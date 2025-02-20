/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.dsl.CommonTempfolderConfig
import at.released.tempfolder.dsl.TempfolderBasePath.Auto
import at.released.tempfolder.dsl.TempfolderBasePath.Path
import at.released.tempfolder.sync.path.WindowsTempDirectoryConfig
import at.released.tempfolder.sync.path.WindowsTempDirectoryConfig.Companion.auto
import at.released.tempfolder.sync.path.WindowsTempDirectoryConfig.Companion.path
import at.released.tempfolder.sync.path.createWindowsTempDirectory

internal actual fun createPlatformTempFolder(config: CommonTempfolderConfig): Tempfolder<*> {
    return Tempfolder.createWindowsTempDirectory {
        setFromCommon(config)
    }
}

private fun WindowsTempDirectoryConfig.setFromCommon(commonConfig: CommonTempfolderConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> auto()
        is Path -> path(commonBase.path)
    }
    prefix = commonConfig.prefix
    permissions = commonConfig.permissions
}
