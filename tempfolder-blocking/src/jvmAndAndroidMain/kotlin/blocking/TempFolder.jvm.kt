/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.dsl.CommonTempfolderConfig
import at.released.tempfolder.dsl.TempfolderBasePath.Auto
import at.released.tempfolder.dsl.TempfolderBasePath.Path
import java.nio.file.Path as NioPath

internal actual fun createPlatformTempFolder(config: CommonTempfolderConfig): Tempfolder<*> {
    return NioTempDirectory {
        setFromCommon(config)
    }
}

private fun NioTempDirectoryConfig.setFromCommon(commonConfig: CommonTempfolderConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> NioTempDirectoryBase.Auto()
        is Path -> NioTempDirectoryBase.Path(NioPath.of(commonBase.path))
    }
}
