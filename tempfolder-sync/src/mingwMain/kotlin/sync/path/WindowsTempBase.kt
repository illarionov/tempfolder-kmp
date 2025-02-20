/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.path

import at.released.tempfolder.dsl.TempfolderDsl

/**
 * Base directory for the temporary folder on Windows
 */
@TempfolderDsl
public sealed class WindowsTempBase {
    /**
     * The base directory is determined automatically.
     */
    @TempfolderDsl
    public data object Auto : WindowsTempBase()

    /**
     * The base directory is specified by the [path].
     */
    @TempfolderDsl
    public class Path(public val path: String) : WindowsTempBase()
}
