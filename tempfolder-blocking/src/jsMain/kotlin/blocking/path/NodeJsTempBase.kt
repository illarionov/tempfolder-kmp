/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.path

import at.released.tempfolder.dsl.TempfolderDsl

/**
 * Base directory for the temporary folder on NodeJS
 */
@TempfolderDsl
public sealed class NodeJsTempBase {
    /**
     * The base directory is determined automatically.
     */
    @TempfolderDsl
    public data object Auto : NodeJsTempBase()

    /**
     * The base directory is specified by the [path].
     */
    @TempfolderDsl
    public class Path(public val path: String) : NodeJsTempBase()
}
