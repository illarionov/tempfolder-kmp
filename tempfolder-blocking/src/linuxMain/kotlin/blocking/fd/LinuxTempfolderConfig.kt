/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.dsl.AdvisoryLockType
import at.released.tempfolder.dsl.AdvisoryLockType.SHARED
import at.released.tempfolder.dsl.CommonTempfolderConfig.Companion.DEFAULT_PERMISSIONS
import at.released.tempfolder.dsl.CommonTempfolderConfig.Companion.DEFAULT_PREFIX
import at.released.tempfolder.dsl.TempfolderDsl
import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.dsl.TempfolderPosixBasePath

@TempfolderDsl
public class LinuxTempfolderConfig internal constructor() {
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

    /**
     * The type of advisory lock to be set on an open temporarily directory to protect  from being deleted
     * by automatic clean-up system such as systemd-tmpfiles.
     */
    public var advisoryLock: AdvisoryLockType = SHARED

    public companion object {
        public fun auto(
            block: TempfolderPosixBasePath.Auto.() -> Unit = {},
        ): TempfolderPosixBasePath = TempfolderPosixBasePath.Auto(block)

        public fun path(path: String): TempfolderPosixBasePath = TempfolderPosixBasePath.Path(path)

        public fun fileDescriptor(fd: Int): TempfolderPosixBasePath = TempfolderPosixBasePath.FileDescriptor(fd)
    }
}
