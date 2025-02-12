/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.path.PosixPathString
import platform.posix.O_DIRECTORY
import platform.posix.O_NOFOLLOW
import platform.posix.O_NONBLOCK

@Throws(TempfolderNativeIOException::class)
internal fun nativeOpenDirectoryAt(
    dirfd: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    resolveBeneath: Boolean = true,
): TempfolderPosixFileDescriptor {
    return platformOpenAt(dirfd, path, 0U, (O_DIRECTORY or O_NOFOLLOW or O_NONBLOCK).toULong(), resolveBeneath)
}

@Throws(TempfolderNativeIOException::class)
internal expect fun platformOpenAt(
    dirfd: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    mode: UInt,
    flags: ULong,
    resolveBeneath: Boolean = true,
): TempfolderPosixFileDescriptor

internal fun interface OpenDirectoryAt {
    operator fun invoke(
        dirfd: TempfolderPosixFileDescriptor,
        path: PosixPathString,
        resolveBeneath: Boolean,
    ): TempfolderPosixFileDescriptor
}
