/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.dsl.CommonTempDirectoryConfig
import at.released.tempfolder.dsl.TempDirectoryBase.Auto
import at.released.tempfolder.dsl.TempDirectoryBase.Path
import at.released.tempfolder.posix200809.dsl.TempDirectoryPosixBase
import at.released.tempfolder.sync.fd.LinuxTempDirectoryConfig
import at.released.tempfolder.sync.fd.createLinuxTempDirectory

internal actual fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*> {
    return TempDirectory.createLinuxTempDirectory {
        setFromCommon(config)
    }
}

private fun LinuxTempDirectoryConfig.setFromCommon(commonConfig: CommonTempDirectoryConfig) {
    base = when (val commonBase = commonConfig.base) {
        is Auto -> TempDirectoryPosixBase.Auto {
            sizeEstimate = commonBase.sizeEstimate
        }

        is Path -> TempDirectoryPosixBase.Path(commonBase.path)
    }
    prefix = commonConfig.prefix
    permissions = commonConfig.permissions
}
