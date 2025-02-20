/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempDirectoryDescriptor
import kotlinx.cinterop.CPointer
import platform.posix.close
import platform.posix.dirent
import platform.posix.errno

internal expect val platformDirent: PlatformDirent<*>

/**
 * #include <dirent.h>
 */
internal interface PlatformDirent<D> {
    fun fdopendir(fd: TempDirectoryDescriptor): D?
    fun closedir(dirp: D): Int
    fun dirfd(dirp: D): Int
    fun readdir(dirp: D): CPointer<dirent>?
    fun rewinddir(dirp: D)
}

@Throws(TempDirectoryNativeIOException::class)
internal fun <D> PlatformDirent<D>.openDirectoryStreamOrCloseFd(dirfd: TempDirectoryDescriptor): D {
    val dir: D? = fdopendir(dirfd)
    if (dir == null) {
        val opendirException = TempDirectoryNativeIOException(errno, "Can not open directory. ${errnoDescription()}`")
        if (close(dirfd.fd) == -1) {
            opendirException.addSuppressed(
                TempDirectoryNativeIOException(errno, "Can not close descriptor. ${errnoDescription()}`"),
            )
        }
        throw opendirException
    }
    rewinddir(dir)
    return dir
}
