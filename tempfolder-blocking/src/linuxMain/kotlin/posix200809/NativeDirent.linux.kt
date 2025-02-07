/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package at.released.tempfolder.posix200809

import kotlinx.cinterop.CPointer
import platform.posix.DIR
import platform.posix.dirent

internal actual class DirP(val raw: CPointer<DIR>)
internal actual fun fdopendir(fd: Int): DirP? = platform.posix.fdopendir(fd)?.let(::DirP)
internal actual fun closedir(dirp: DirP): Int = platform.posix.closedir(dirp.raw)
internal actual fun dirfd(dirp: DirP): Int = platform.posix.dirfd(dirp.raw)
internal actual fun readdir(dirp: DirP): CPointer<dirent>? = platform.posix.readdir(dirp.raw)
internal actual fun rewinddir(dirp: DirP) = platform.posix.rewinddir(dirp.raw)
