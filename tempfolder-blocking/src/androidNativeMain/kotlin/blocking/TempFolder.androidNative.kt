/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.blocking.fd.AndroidNativeTempDirectoryConfig
import at.released.tempfolder.blocking.fd.createAndroidNativeTempDirectory
import at.released.tempfolder.dsl.CommonTempfolderConfig
import at.released.tempfolder.dsl.TempfolderBasePath.Auto
import at.released.tempfolder.dsl.TempfolderBasePath.Path
import at.released.tempfolder.posix200809.dsl.TempfolderPosixBasePath

internal actual fun createPlatformTempFolder(config: CommonTempfolderConfig): Tempfolder<*> {
    return Tempfolder.createAndroidNativeTempDirectory {
        setFromCommon(config)
    }
}

private fun AndroidNativeTempDirectoryConfig.setFromCommon(commonConfig: CommonTempfolderConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> TempfolderPosixBasePath.Auto {
            sizeEstimate = commonBase.sizeEstimate
        }

        is Path -> TempfolderPosixBasePath.Path(commonBase.path)
    }
    prefix = commonConfig.prefix
    permissions = commonConfig.permissions
}
