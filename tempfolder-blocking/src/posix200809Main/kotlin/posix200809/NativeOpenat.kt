/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.path.PosixPathString

@Throws(TempfolderNativeIOException::class)
internal expect fun nativeOpenDirectoryAt(
    dirfd: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    resolveBeneath: Boolean = true,
): TempfolderPosixFileDescriptor

internal fun interface OpenDirectoryAt {
    operator fun invoke(
        dirfd: TempfolderPosixFileDescriptor,
        path: PosixPathString,
        resolveBeneath: Boolean,
    ): TempfolderPosixFileDescriptor
}
