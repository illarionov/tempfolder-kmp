/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.dsl.CommonTempDirectoryConfig.Companion.DEFAULT_PERMISSIONS
import at.released.tempfolder.dsl.CommonTempDirectoryConfig.Companion.DEFAULT_PREFIX
import at.released.tempfolder.dsl.TempDirectoryDsl
import at.released.tempfolder.dsl.TempDirectoryFileModeBit

@TempDirectoryDsl
public class NodeTempDirectoryConfig {
    /**
     * Base path for the temporary directory
     */
    public var base: TempDirectoryNodeBase = auto()

    /**
     * Prefix for the directory name.
     *
     * Default: "tempfolder-".
     */
    public var prefix: String = DEFAULT_PREFIX

    /**
     * Permissions for the created directory in the form of POSIX file mode bits.
     *
     * Default: 0700
     */
    public var permissions: Set<TempDirectoryFileModeBit> = DEFAULT_PERMISSIONS

    public companion object {
        public fun NodeTempDirectoryConfig.auto(): TempDirectoryNodeBase.Auto = TempDirectoryNodeBase.Auto

        public fun NodeTempDirectoryConfig.path(path: String): TempDirectoryNodeBase.Path =
            TempDirectoryNodeBase.Path(path)
    }
}
