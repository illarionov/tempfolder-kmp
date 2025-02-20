/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

import at.released.tempfolder.wasip1.type.Errno

public class TempDirectoryWasiIOException : TempDirectoryIOException {
    public val wasiErrno: Int

    @Suppress("WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR")
    public val name: String get() = Errno.fromCode(wasiErrno)?.name ?: wasiErrno.toString()

    public constructor(errno: Int) : super() {
        this.wasiErrno = errno
    }

    public constructor(errno: Int, message: String?) : super(message) {
        this.wasiErrno = errno
    }

    public constructor(errno: Int, cause: Throwable) : super(cause) {
        this.wasiErrno = errno
    }

    public constructor(errno: Int, message: String, cause: Throwable?) : super(message, cause) {
        this.wasiErrno = errno
    }

    internal companion object {
        fun prefixed(errno: Int, prefix: String?) =
            TempDirectoryWasiIOException(errno, "$prefix. Error ${Errno.nameFromCode(errno)}")
    }
}
