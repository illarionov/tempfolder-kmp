/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.wasip1

import at.released.tempfolder.testframework.wasip1.type.Filestat
import at.released.tempfolder.testframework.wasip1.type.Filestat.Companion
import at.released.tempfolder.wasip1.type.Filetype
import kotlin.wasm.unsafe.Pointer

internal const val FILESTAT_PACKED_SIZE = 64

internal fun Companion.readFrom(pointer: Pointer): Filestat {
    return Filestat(
        dev = pointer.loadLong().toULong(),
        ino = (pointer + 8).loadLong().toULong(),
        filetype = (pointer + 16).loadByte()
            .let { Filetype.fromCode(it.toInt()) ?: error("Unknown filetype code`$it`") },
        nlink = (pointer + 24).loadLong().toULong(),
        size = (pointer + 32).loadLong().toULong(),
        atim = (pointer + 40).loadLong().toULong(),
        mtim = (pointer + 48).loadLong().toULong(),
        ctim = (pointer + 56).loadLong().toULong(),
    )
}
