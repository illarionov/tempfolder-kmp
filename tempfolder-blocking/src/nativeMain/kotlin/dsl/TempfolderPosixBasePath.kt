/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")

package at.released.tempfolder.dsl

import at.released.tempfolder.dsl.TempfolderSizeEstimate.SMALL

/**
 * Base directory for the temporary folder for POSIX file systems
 */
@TempfolderDsl
public sealed interface TempfolderPosixBasePath {
    /**
     * The base directory is determined automatically.
     *
     * On Unix-like systems, it uses the TEMPDIR environment variable, or defaults to `/tmp` or `/var/tmp`
     * depending on the [sizeEstimate].
     */
    @TempfolderDsl
    public class Auto private constructor() : TempfolderBasePath.Auto, TempfolderPosixBasePath {
        override var sizeEstimate: TempfolderSizeEstimate = SMALL

        public companion object {
            public operator fun invoke(
                block: Auto.() -> Unit = {},
            ): Auto = Auto().apply(block)
        }
    }

    /**
     * The base directory is specified by the [path].
     */
    @TempfolderDsl
    public data class Path(val path: String) : TempfolderPosixBasePath

    /**
     * The base directory is specified by opened file descriptor referring to a directory.
     */
    @TempfolderDsl
    public value class FileDescriptor(public val fd: Int) : TempfolderPosixBasePath
}
