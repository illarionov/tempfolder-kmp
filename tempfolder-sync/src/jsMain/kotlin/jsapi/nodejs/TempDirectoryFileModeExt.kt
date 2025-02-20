/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.jsapi.nodejs

import at.released.tempfolder.dsl.TempDirectoryFileModeBit

internal fun TempDirectoryFileModeBit.Companion.fromNodeJsMode(mode: Int): Set<TempDirectoryFileModeBit> {
    return TempDirectoryFileModeBit.entries.mapNotNullTo(mutableSetOf()) {
        if (mode and it.nodejsMask == it.nodejsMask) it else null
    }
}

internal fun Set<TempDirectoryFileModeBit>.toNodeJsMode(): Int = fold(0) { mask, bit -> mask or bit.nodejsMask }

private val TempDirectoryFileModeBit.nodejsMask: Int
    get() = when (this) {
        TempDirectoryFileModeBit.SUID -> 0b100_000_000_000
        TempDirectoryFileModeBit.SGID -> 0b010_000_000_000
        TempDirectoryFileModeBit.STICKY -> 0b001_000_000_000
        TempDirectoryFileModeBit.USER_READ -> 0b000_100_000_000
        TempDirectoryFileModeBit.USER_WRITE -> 0b000_010_000_000
        TempDirectoryFileModeBit.USER_EXECUTE -> 0b000_001_000_000
        TempDirectoryFileModeBit.GROUP_READ -> 0b000_000_100_000
        TempDirectoryFileModeBit.GROUP_WRITE -> 0b000_000_010_000
        TempDirectoryFileModeBit.GROUP_EXECUTE -> 0b000_000_001_000
        TempDirectoryFileModeBit.OTHER_READ -> 0b000_000_000_100
        TempDirectoryFileModeBit.OTHER_WRITE -> 0b000_000_000_010
        TempDirectoryFileModeBit.OTHER_EXECUTE -> 0b000_000_000_001
    }
