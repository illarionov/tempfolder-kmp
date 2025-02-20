/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")

package at.released.tempfolder.posix200809.dsl

import at.released.tempfolder.dsl.TempDirectoryBase
import at.released.tempfolder.dsl.TempDirectoryDsl
import at.released.tempfolder.dsl.TempDirectorySizeEstimate
import at.released.tempfolder.dsl.TempDirectorySizeEstimate.SMALL

/**
 * Base directory for the temporary directory on POSIX file systems
 */
@TempDirectoryDsl
public sealed interface TempDirectoryPosixBase {
    /**
     * The base directory is determined automatically.
     *
     * On Unix-like systems, it uses the TEMPDIR environment variable, or defaults to `/tmp` or `/var/tmp`
     * depending on the [sizeEstimate].
     */
    @TempDirectoryDsl
    public class Auto private constructor() : TempDirectoryBase.Auto, TempDirectoryPosixBase {
        override var sizeEstimate: TempDirectorySizeEstimate = SMALL

        public companion object {
            public operator fun invoke(
                block: Auto.() -> Unit = {},
            ): Auto = Auto().apply(block)
        }
    }

    /**
     * The base directory is specified by the [path].
     */
    @TempDirectoryDsl
    public data class Path(val path: String) : TempDirectoryPosixBase

    /**
     * The base directory is specified by opened file descriptor referring to a directory.
     */
    @TempDirectoryDsl
    public value class FileDescriptor(public val fd: Int) : TempDirectoryPosixBase
}
