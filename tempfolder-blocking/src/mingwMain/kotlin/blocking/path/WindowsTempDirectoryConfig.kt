/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.path

import at.released.tempfolder.dsl.CommonTempfolderConfig.Companion.DEFAULT_PERMISSIONS
import at.released.tempfolder.dsl.CommonTempfolderConfig.Companion.DEFAULT_PREFIX
import at.released.tempfolder.dsl.TempfolderDsl
import at.released.tempfolder.dsl.TempfolderFileModeBit

@TempfolderDsl
public class WindowsTempDirectoryConfig {
    /**
     * Base path for the temporary directory
     */
    public var base: WindowsTempBase = auto()

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
    public var permissions: Set<TempfolderFileModeBit> = DEFAULT_PERMISSIONS

    public companion object {
        public fun WindowsTempDirectoryConfig.auto(): WindowsTempBase.Auto = WindowsTempBase.Auto

        public fun WindowsTempDirectoryConfig.path(path: String): WindowsTempBase.Path = WindowsTempBase.Path(path)
    }
}
