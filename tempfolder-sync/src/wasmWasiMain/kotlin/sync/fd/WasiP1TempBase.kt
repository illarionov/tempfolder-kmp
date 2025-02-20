/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.fd

import at.released.tempfolder.dsl.TempfolderDsl

/**
 * Base directory for the temporary folder on Wasm WASI Preview 1 File system
 */
@TempfolderDsl
public sealed interface WasiP1TempBase {
    /**
     * The base directory is determined automatically.
     */
    @TempfolderDsl
    public data object Auto : WasiP1TempBase

    /**
     * The base directory is specified by the [path] resolved from preopened directories.
     */
    @TempfolderDsl
    public class Path(public val path: String) : WasiP1TempBase

    /**
     * The base directory is specified by opened file descriptor referring to a directory.
     */
    @TempfolderDsl
    public class FileDescriptor(public val fd: Int) : WasiP1TempBase
}
