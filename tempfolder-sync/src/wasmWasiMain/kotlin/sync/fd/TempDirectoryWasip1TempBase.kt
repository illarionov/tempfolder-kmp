/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.fd

import at.released.tempfolder.dsl.TempDirectoryDsl

/**
 * Base directory for the temporary folder on Wasm WASI Preview 1 File system
 */
@TempDirectoryDsl
public sealed interface TempDirectoryWasip1TempBase {
    /**
     * The base directory is determined automatically.
     */
    @TempDirectoryDsl
    public data object Auto : TempDirectoryWasip1TempBase

    /**
     * The base directory is specified by the [path] resolved from preopened directories.
     */
    @TempDirectoryDsl
    public class Path(public val path: String) : TempDirectoryWasip1TempBase

    /**
     * The base directory is specified by opened file descriptor referring to a directory.
     */
    @TempDirectoryDsl
    public class FileDescriptor(public val fd: Int) : TempDirectoryWasip1TempBase
}
