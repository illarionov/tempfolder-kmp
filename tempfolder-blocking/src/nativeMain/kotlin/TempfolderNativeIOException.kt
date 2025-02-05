/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

public class TempfolderNativeIOException : TempfolderIOException {
    public val errno: Int

    public constructor(errno: Int) : super() {
        this.errno = errno
    }
    public constructor(errno: Int, message: String?) : super(message) {
        this.errno = errno
    }
    public constructor(errno: Int, cause: Throwable) : super(cause) {
        this.errno = errno
    }
    public constructor(errno: Int, message: String, cause: Throwable?) : super(message, cause) {
        this.errno = errno
    }
}
