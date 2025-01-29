/*
 * Copyright 2024-2025, Alexey Illarionov and the at-released-tempfolder project contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
