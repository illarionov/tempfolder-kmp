/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.jsapi.nodejs

import at.released.tempfolder.dsl.TempfolderFileModeBit

internal fun TempfolderFileModeBit.Companion.fromNodeJsMode(mode: Int): Set<TempfolderFileModeBit> {
    return TempfolderFileModeBit.entries.mapNotNullTo(mutableSetOf()) {
        if (mode and it.nodejsMask == it.nodejsMask) it else null
    }
}

internal fun Set<TempfolderFileModeBit>.toNodeJsMode(): Int = fold(0) { mask, bit -> mask or bit.nodejsMask }

private val TempfolderFileModeBit.nodejsMask: Int
    get() = when (this) {
        TempfolderFileModeBit.SUID -> 0b100_000_000_000
        TempfolderFileModeBit.SGID -> 0b010_000_000_000
        TempfolderFileModeBit.STICKY -> 0b001_000_000_000
        TempfolderFileModeBit.USER_READ -> 0b000_100_000_000
        TempfolderFileModeBit.USER_WRITE -> 0b000_010_000_000
        TempfolderFileModeBit.USER_EXECUTE -> 0b000_001_000_000
        TempfolderFileModeBit.GROUP_READ -> 0b000_000_100_000
        TempfolderFileModeBit.GROUP_WRITE -> 0b000_000_010_000
        TempfolderFileModeBit.GROUP_EXECUTE -> 0b000_000_001_000
        TempfolderFileModeBit.OTHER_READ -> 0b000_000_000_100
        TempfolderFileModeBit.OTHER_WRITE -> 0b000_000_000_010
        TempfolderFileModeBit.OTHER_EXECUTE -> 0b000_000_000_001
    }
