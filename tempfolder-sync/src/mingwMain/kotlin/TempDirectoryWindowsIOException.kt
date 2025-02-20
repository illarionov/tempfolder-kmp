/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

import at.released.tempfolder.winapi.errcode.Win32ErrorCode

public class TempDirectoryWindowsIOException : TempDirectoryIOException {
    public val lastError: UInt

    public constructor(lastError: UInt) : super() {
        this.lastError = lastError
    }

    public constructor(message: String, lastError: UInt, cause: Throwable?) : super(message, cause) {
        this.lastError = lastError
    }

    public companion object {
        internal operator fun invoke(
            messagePrefix: String,
            lastError: Win32ErrorCode = Win32ErrorCode.last(),
            cause: Throwable? = null,
        ): TempDirectoryWindowsIOException {
            val messageDescription = ": ${lastError.description()}"
            return TempDirectoryWindowsIOException("$messagePrefix$messageDescription", lastError.code, cause)
        }
    }
}
