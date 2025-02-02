/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.dsl

import at.released.tempfolder.dsl.TempfolderSizeEstimate.SMALL

/**
 * Base directory for the temporary folder
 */
@TempfolderDsl
public sealed interface TempfolderBasePath {
    /**
     * The base directory is specified by the platform-specific [path].
     */
    @TempfolderDsl
    public data class Path(val path: String) : TempfolderBasePath

    /**
     * The base directory is determined automatically.
     *
     * On Unix-like systems, it uses the TEMPDIR environment variable, or defaults to `/tmp` or `/var/tmp`
     * depending on the [sizeEstimate].
     *
     * On Windows, the result of the `GetTempPath()` method will be used.
     */
    @TempfolderDsl
    public class Auto private constructor() : TempfolderBasePath {
        /**
         * Estimated size of files in the temporary directory used to determine the base directory
         * on Unix-like operating systems if the TEMPDIR environment variable is not set.
         */
        public var sizeEstimate: TempfolderSizeEstimate = SMALL

        public companion object {
            public operator fun invoke(
                block: Auto.() -> Unit = {},
            ): TempfolderBasePath = Auto().apply(block)
        }
    }
}
