/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.nativefunc

import at.released.tempfolder.TempfolderNativeIOException
import platform.posix.close
import platform.posix.errno

internal class CloseableFileDescriptor(
    val fd: Int,
) : AutoCloseable {
    override fun close() {
        if (close(fd) == -1) {
            throw TempfolderNativeIOException(
                errno,
                "Can not close descriptor. ${errnoDescription()}`",
            )
        }
    }
}
