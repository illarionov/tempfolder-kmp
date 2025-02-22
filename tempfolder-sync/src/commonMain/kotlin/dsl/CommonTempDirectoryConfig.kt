/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.dsl

import at.released.tempfolder.dsl.TempDirectoryFileModeBit.USER_EXECUTE
import at.released.tempfolder.dsl.TempDirectoryFileModeBit.USER_READ
import at.released.tempfolder.dsl.TempDirectoryFileModeBit.USER_WRITE

/**
 * Configuration of the created temporary directory
 */
@TempDirectoryDsl
public class CommonTempDirectoryConfig internal constructor() {
    /**
     * Root path for the temporary directory.
     *
     * Use [auto()][auto] to automatically select the best location or [path()][path] to specify a custom path.
     */
    public var base: TempDirectoryBase = auto()

    /**
     * Prefix used for naming the directory
     *
     * Default: "tempfolder-".
     */
    public var prefix: String = DEFAULT_PREFIX

    /**
     * Permissions for the created directory in the form of POSIX file mode bits.
     *
     * May not be supported by the implementation.
     *
     * Default: 0700 (USER_READ, USER_WRITE, USER_EXECUTE)
     */
    public var permissions: Set<TempDirectoryFileModeBit> = DEFAULT_PERMISSIONS

    public companion object {
        internal const val DEFAULT_PREFIX = "tempfolder-"
        internal val DEFAULT_PERMISSIONS = setOf(USER_READ, USER_WRITE, USER_EXECUTE)

        public fun CommonTempDirectoryConfig.auto(
            block: TempDirectoryBase.Auto.() -> Unit = {},
        ): TempDirectoryBase = TempDirectoryBase.Auto(block)

        public fun CommonTempDirectoryConfig.path(path: String): TempDirectoryBase = TempDirectoryBase.Path(path)
    }
}
