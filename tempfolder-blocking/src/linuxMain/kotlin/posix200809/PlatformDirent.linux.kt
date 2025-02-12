/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package at.released.tempfolder.posix200809

import kotlinx.cinterop.CPointer
import platform.posix.DIR
import platform.posix.dirent

internal actual val platformDirent: PlatformDirent<*> = LinuxDirent

private object LinuxDirent : PlatformDirent<CPointer<DIR>> {
    override fun fdopendir(fd: TempfolderPosixFileDescriptor): CPointer<DIR>? = platform.posix.fdopendir(fd.linuxFd)
    override fun rewinddir(dirp: CPointer<DIR>) = platform.posix.rewinddir(dirp)
    override fun readdir(dirp: CPointer<DIR>): CPointer<dirent>? = platform.posix.readdir(dirp)
    override fun dirfd(dirp: CPointer<DIR>): Int = platform.posix.dirfd(dirp)
    override fun closedir(dirp: CPointer<DIR>): Int = platform.posix.closedir(dirp)
}
