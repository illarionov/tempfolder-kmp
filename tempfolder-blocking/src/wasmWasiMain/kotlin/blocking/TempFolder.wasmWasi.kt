/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.blocking.fd.WasiP1TempBase
import at.released.tempfolder.blocking.fd.Wasip1TempDirectoryConfig
import at.released.tempfolder.blocking.fd.createWasiP1TempDirectory
import at.released.tempfolder.dsl.CommonTempfolderConfig
import at.released.tempfolder.dsl.TempfolderBasePath.Auto
import at.released.tempfolder.dsl.TempfolderBasePath.Path

internal actual fun createPlatformTempFolder(config: CommonTempfolderConfig): Tempfolder<*> {
    return Tempfolder.createWasiP1TempDirectory {
        setFromCommon(config)
    }
}

private fun Wasip1TempDirectoryConfig.setFromCommon(commonConfig: CommonTempfolderConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> WasiP1TempBase.Auto
        is Path -> WasiP1TempBase.Path(commonBase.path)
    }
    prefix = commonConfig.prefix
}
