/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package at.released.tempfolder.posix200809

import kotlinx.cinterop.CPointer
import platform.posix.dirent

internal actual val platformDirent: NativeDirent<*> = AndroidDirent

private object AndroidDirent : NativeDirent<CPointer<cnames.structs.DIR>> {
    override fun fdopendir(fd: Int): CPointer<cnames.structs.DIR>? = platform.posix.fdopendir(fd)
    override fun rewinddir(dirp: CPointer<cnames.structs.DIR>) = platform.posix.rewinddir(dirp)
    override fun readdir(dirp: CPointer<cnames.structs.DIR>): CPointer<dirent>? = platform.posix.readdir(dirp)
    override fun dirfd(dirp: CPointer<cnames.structs.DIR>): Int = platform.posix.dirfd(dirp)
    override fun closedir(dirp: CPointer<cnames.structs.DIR>): Int = platform.posix.closedir(dirp)
}
