/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.dsl

import at.released.tempfolder.dsl.TempDirectorySizeEstimate.SMALL

/**
 * Base directory for the temporary folder
 */
@TempDirectoryDsl
public sealed interface TempDirectoryBase {
    /**
     * The base directory is specified by the platform-specific [path].
     */
    @TempDirectoryDsl
    public data class Path(val path: String) : TempDirectoryBase

    /**
     * The base directory is determined automatically.
     *
     * On Unix-like systems, it uses the TEMPDIR environment variable, or defaults to `/tmp` or `/var/tmp`
     * depending on the [sizeEstimate].
     *
     * On Windows, the result of the `GetTempPath()` method will be used.
     */
    @TempDirectoryDsl
    public interface Auto : TempDirectoryBase {
        /**
         * Estimated size of files in the temporary directory used to determine the base directory
         * on Unix-like operating systems if the TEMPDIR environment variable is not set.
         */
        public var sizeEstimate: TempDirectorySizeEstimate

        public companion object {
            public operator fun invoke(
                block: Auto.() -> Unit = {},
            ): TempDirectoryBase = object : Auto {
                override var sizeEstimate: TempDirectorySizeEstimate = SMALL
            }.apply(block)
        }
    }
}
