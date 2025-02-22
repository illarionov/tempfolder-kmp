/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.dsl.CommonTempDirectoryConfig.Companion.DEFAULT_PREFIX
import at.released.tempfolder.dsl.TempDirectoryDsl

@TempDirectoryDsl
public class WindowsTempDirectoryConfig internal constructor() {
    /**
     * Base path for the temporary directory
     */
    public var base: TempDirectoryWindowsBase = auto()

    /**
     * Prefix for the directory name.
     *
     * Default: "tempfolder-".
     */
    public var prefix: String = DEFAULT_PREFIX

    public companion object {
        public fun WindowsTempDirectoryConfig.auto(): TempDirectoryWindowsBase.Auto = TempDirectoryWindowsBase.Auto

        public fun WindowsTempDirectoryConfig.path(path: String): TempDirectoryWindowsBase.Path =
            TempDirectoryWindowsBase.Path(path)
    }
}
