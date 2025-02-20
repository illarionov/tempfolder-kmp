/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.dsl.TempDirectoryDsl

/**
 * Base directory for the temporary folder on NodeJS
 */
@TempDirectoryDsl
public sealed class TempDirectoryNodeBase {
    /**
     * The base directory is determined automatically.
     */
    @TempDirectoryDsl
    public data object Auto : TempDirectoryNodeBase()

    /**
     * The base directory is specified by the [path].
     */
    @TempDirectoryDsl
    public class Path(public val path: String) : TempDirectoryNodeBase()
}
