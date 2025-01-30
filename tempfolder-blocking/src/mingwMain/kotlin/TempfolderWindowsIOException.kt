/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

public class TempfolderWindowsIOException : TempfolderIOException {
    public val lastError: UInt?

    public constructor(lastError: UInt? = null) : super() {
        this.lastError = lastError
    }

    public constructor(messagePrefix: String, lastError: UInt? = null) : super(
        messagePrefix + (lastError?.let { ": 0x${it.toString(16)}" } ?: ""),
    ) {
        this.lastError = lastError
    }
}
