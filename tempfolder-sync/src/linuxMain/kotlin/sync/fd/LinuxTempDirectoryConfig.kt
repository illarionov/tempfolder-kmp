/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.fd

import at.released.tempfolder.dsl.CommonTempDirectoryConfig.Companion.DEFAULT_PERMISSIONS
import at.released.tempfolder.dsl.CommonTempDirectoryConfig.Companion.DEFAULT_PREFIX
import at.released.tempfolder.dsl.TempDirectoryDsl
import at.released.tempfolder.dsl.TempDirectoryFileModeBit
import at.released.tempfolder.posix200809.dsl.TempDirectoryAdvisoryLockType
import at.released.tempfolder.posix200809.dsl.TempDirectoryAdvisoryLockType.SHARED
import at.released.tempfolder.posix200809.dsl.TempDirectoryPosixBase

@TempDirectoryDsl
public class LinuxTempDirectoryConfig internal constructor() {
    /**
     * Base path for the temporary directory
     */
    public var base: TempDirectoryPosixBase = TempDirectoryPosixBase.Auto()

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
    public var permissions: Set<TempDirectoryFileModeBit> = DEFAULT_PERMISSIONS

    /**
     * The type of advisory lock to be set on an open temporarily directory to protect  from being deleted
     * by automatic clean-up system such as systemd-tmpfiles.
     */
    public var advisoryLock: TempDirectoryAdvisoryLockType = SHARED

    public companion object {
        public fun LinuxTempDirectoryConfig.auto(
            block: TempDirectoryPosixBase.Auto.() -> Unit = {},
        ): TempDirectoryPosixBase = TempDirectoryPosixBase.Auto(block)

        public fun LinuxTempDirectoryConfig.path(path: String): TempDirectoryPosixBase =
            TempDirectoryPosixBase.Path(path)

        public fun LinuxTempDirectoryConfig.fileDescriptor(fd: Int): TempDirectoryPosixBase =
            TempDirectoryPosixBase.FileDescriptor(fd)
    }
}
