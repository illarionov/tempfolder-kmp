/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.platform.apple.mkdirat
import at.released.tempfolder.posix200809.path.allocNullTerminatedPath
import kotlinx.cinterop.memScoped
import platform.posix.errno

internal actual fun platformMkdirat(
    base: TempfolderPosixFileDescriptor,
    directoryName: PosixPathString,
    mode: UInt,
): Int = memScoped {
    val pathBytes = allocNullTerminatedPath(directoryName)
    val result = mkdirat(base.fd, pathBytes, mode)
    return if (result != -1) {
        0
    } else {
        errno
    }
}
