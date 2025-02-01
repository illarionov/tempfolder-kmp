/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.winapi.errcode

import platform.windows.GetLastError

internal value class Win32ErrorCode(
    val code: UInt,
) {
    fun description(): String = "0x${code.toString(16).padStart(8, '0')} `${getErrorMessage()}`"

    override fun toString(): String {
        return "Win32Error(${description()})"
    }

    public companion object {
        fun last(): Win32ErrorCode = Win32ErrorCode(GetLastError())
    }
}
