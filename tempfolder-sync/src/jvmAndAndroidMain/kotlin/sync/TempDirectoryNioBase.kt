/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.dsl.TempDirectoryDsl
import java.nio.file.FileSystem
import java.nio.file.FileSystems

@TempDirectoryDsl
public sealed class TempDirectoryNioBase {
    /**
     * The base directory is determined automatically.
     */
    @TempDirectoryDsl
    public class Auto(
        public var fileSystem: FileSystem = FileSystems.getDefault(),
    ) : TempDirectoryNioBase()

    /**
     * The base directory is specified by the [path].
     */
    @TempDirectoryDsl
    public class Path(public var path: java.nio.file.Path) : TempDirectoryNioBase()
}
