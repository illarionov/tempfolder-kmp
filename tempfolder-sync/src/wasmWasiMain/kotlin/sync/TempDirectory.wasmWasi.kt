/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.dsl.CommonTempDirectoryConfig
import at.released.tempfolder.dsl.TempDirectoryBase.Auto
import at.released.tempfolder.dsl.TempDirectoryBase.Path
import at.released.tempfolder.sync.fd.TempDirectoryWasip1TempBase
import at.released.tempfolder.sync.fd.Wasip1TempDirectoryConfig
import at.released.tempfolder.sync.fd.createWasip1TempDirectory

internal actual fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*> {
    return TempDirectory.createWasip1TempDirectory {
        setFromCommon(config)
    }
}

private fun Wasip1TempDirectoryConfig.setFromCommon(commonConfig: CommonTempDirectoryConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> TempDirectoryWasip1TempBase.Auto
        is Path -> TempDirectoryWasip1TempBase.Path(commonBase.path)
    }
    prefix = commonConfig.prefix
}
