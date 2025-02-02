/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.dsl

import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_EXECUTE
import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_READ
import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_WRITE

/**
 * Configuration of the created temporary directory
 */
@TempfolderDsl
public class TempfolderCommonConfigBlock internal constructor() {
    /**
     * Base path for the temporary directory
     */
    public var base: TempfolderBasePath = auto()

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
        internal const val DEFAULT_PREFIX = "tempfolder-"
        internal val DEFAULT_PERMISSIONS = setOf(USER_READ, USER_WRITE, USER_EXECUTE)

        public fun auto(
            block: TempfolderBasePath.Auto.() -> Unit = {},
        ): TempfolderBasePath = TempfolderBasePath.Auto(block)

        public fun path(path: String): TempfolderBasePath = TempfolderBasePath.Path(path)
    }
}
