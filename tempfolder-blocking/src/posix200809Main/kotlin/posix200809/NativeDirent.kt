/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package at.released.tempfolder.posix200809

import kotlinx.cinterop.CPointer
import platform.posix.dirent

internal expect class DirP
internal expect fun fdopendir(fd: Int): DirP?
internal expect fun closedir(dirp: DirP): Int
internal expect fun dirfd(dirp: DirP): Int
internal expect fun readdir(dirp: DirP): CPointer<dirent>?
internal expect fun rewinddir(dirp: DirP)
