/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.dsl.CommonTempfolderConfig.Companion.DEFAULT_PERMISSIONS
import at.released.tempfolder.dsl.CommonTempfolderConfig.Companion.DEFAULT_PREFIX
import at.released.tempfolder.dsl.TempfolderDsl
import at.released.tempfolder.dsl.TempfolderFileModeBit
import java.nio.file.Path

@TempfolderDsl
public class NioTempDirectoryConfig internal constructor() {
    /**
     * Base path for the temporary directory
     */
    public var base: NioTempBase = NioTempBase.Auto()

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
        public fun auto(
            block: NioTempBase.Auto.() -> Unit = {},
        ): NioTempBase = NioTempBase.Auto().apply(block)

        public fun path(path: Path): NioTempBase = NioTempBase.Path(path)
    }
}
