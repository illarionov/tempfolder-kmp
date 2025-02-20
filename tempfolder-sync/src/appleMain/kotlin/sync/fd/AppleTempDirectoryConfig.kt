/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.fd

import at.released.tempfolder.dsl.CommonTempfolderConfig.Companion.DEFAULT_PERMISSIONS
import at.released.tempfolder.dsl.CommonTempfolderConfig.Companion.DEFAULT_PREFIX
import at.released.tempfolder.dsl.TempfolderDsl
import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.posix200809.dsl.TempfolderPosixBasePath

@TempfolderDsl
public class AppleTempDirectoryConfig internal constructor() {
    /**
     * Base path for the temporary directory
     */
    public var base: TempfolderPosixBasePath = TempfolderPosixBasePath.Auto()

    /**
     * Prefix for the directory name.
     *
     * Default: "tempfolder-".
     */
    public var prefix: String = DEFAULT_PREFIX

    /**
     * Permissions for the created directory in the form of POSIX file mode bits.
     *
     * Default: 0700
     */
    public var permissions: Set<TempfolderFileModeBit> = DEFAULT_PERMISSIONS

    public companion object {
        public fun AppleTempDirectoryConfig.auto(
            block: TempfolderPosixBasePath.Auto.() -> Unit = {},
        ): TempfolderPosixBasePath = TempfolderPosixBasePath.Auto(block)

        public fun AppleTempDirectoryConfig.path(path: String): TempfolderPosixBasePath =
            TempfolderPosixBasePath.Path(path)

        public fun AppleTempDirectoryConfig.fileDescriptor(fd: Int): TempfolderPosixBasePath =
            TempfolderPosixBasePath.FileDescriptor(fd)
    }
}
