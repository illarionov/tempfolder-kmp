/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.dsl.TempfolderDsl
import java.nio.file.FileSystem
import java.nio.file.FileSystems

@TempfolderDsl
public sealed class NioTempBase {
    /**
     * The base directory is determined automatically.
     */
    @TempfolderDsl
    public class Auto(
        public var fileSystem: FileSystem = FileSystems.getDefault(),
    ) : NioTempBase()

    /**
     * The base directory is specified by the [path].
     */
    @TempfolderDsl
    public class Path(public var path: java.nio.file.Path) : NioTempBase()
}
