/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.path.PosixPath
import platform.posix.O_DIRECTORY
import platform.posix.O_NOFOLLOW
import platform.posix.O_NONBLOCK

@Throws(TempDirectoryNativeIOException::class)
internal fun nativeOpenDirectoryAt(
    dirfd: TempDirectoryDescriptor,
    path: PosixPath,
    resolveBeneath: Boolean = true,
): TempDirectoryDescriptor {
    return platformOpenAt(dirfd, path, 0U, (O_DIRECTORY or O_NOFOLLOW or O_NONBLOCK).toULong(), resolveBeneath)
}

@Throws(TempDirectoryNativeIOException::class)
internal expect fun platformOpenAt(
    dirfd: TempDirectoryDescriptor,
    path: PosixPath,
    mode: UInt,
    flags: ULong,
    resolveBeneath: Boolean = true,
): TempDirectoryDescriptor

internal fun interface OpenDirectoryAt {
    operator fun invoke(
        dirfd: TempDirectoryDescriptor,
        path: PosixPath,
        resolveBeneath: Boolean,
    ): TempDirectoryDescriptor
}
