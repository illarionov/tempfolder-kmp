/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package at.released.tempfolder.posix200809

import kotlinx.cinterop.CPointer
import platform.posix.close
import platform.posix.dirent
import platform.posix.errno

internal expect val platformDirent: PlatformDirent<*>

/**
 * #include <dirent.h>
 */
internal interface PlatformDirent<D> {
    fun fdopendir(fd: TempfolderPosixFileDescriptor): D?
    fun closedir(dirp: D): Int
    fun dirfd(dirp: D): Int
    fun readdir(dirp: D): CPointer<dirent>?
    fun rewinddir(dirp: D)
}

@Throws(TempfolderNativeIOException::class)
internal fun <D> PlatformDirent<D>.openDirectoryStreamOrCloseFd(dirfd: TempfolderPosixFileDescriptor): D {
    val dir: D? = fdopendir(dirfd)
    if (dir == null) {
        val opendirException = TempfolderNativeIOException(errno, "Can not open directory. ${errnoDescription()}`")
        if (close(dirfd.fd) == -1) {
            opendirException.addSuppressed(
                TempfolderNativeIOException(errno, "Can not close descriptor. ${errnoDescription()}`"),
            )
        }
        throw opendirException
    }
    rewinddir(dir)
    return dir
}
