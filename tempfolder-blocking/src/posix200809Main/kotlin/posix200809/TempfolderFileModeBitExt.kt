/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.dsl.TempfolderFileModeBit
import platform.posix.S_IRGRP
import platform.posix.S_IROTH
import platform.posix.S_IRUSR
import platform.posix.S_IWGRP
import platform.posix.S_IWOTH
import platform.posix.S_IWUSR
import platform.posix.S_IXGRP
import platform.posix.S_IXOTH
import platform.posix.S_IXUSR

internal fun Set<TempfolderFileModeBit>.toPosixMode(): UInt = this.fold(0U) { mask, bit -> mask or bit.posixMask }

internal fun TempfolderFileModeBit.Companion.fromPosixMode(mode: UInt): Set<TempfolderFileModeBit> {
    return TempfolderFileModeBit.entries.mapNotNullTo(mutableSetOf()) {
        if (mode and it.posixMask == it.posixMask) it else null
    }
}

private val TempfolderFileModeBit.posixMask: UInt
    get() = when (this) {
        TempfolderFileModeBit.SUID -> 0b100_000_000_000U
        TempfolderFileModeBit.SGID -> 0b010_000_000_000U
        TempfolderFileModeBit.STICKY -> 0b001_000_000_000U
        TempfolderFileModeBit.USER_READ -> S_IRUSR.toUInt()
        TempfolderFileModeBit.USER_WRITE -> S_IWUSR.toUInt()
        TempfolderFileModeBit.USER_EXECUTE -> S_IXUSR.toUInt()
        TempfolderFileModeBit.GROUP_READ -> S_IRGRP.toUInt()
        TempfolderFileModeBit.GROUP_WRITE -> S_IWGRP.toUInt()
        TempfolderFileModeBit.GROUP_EXECUTE -> S_IXGRP.toUInt()
        TempfolderFileModeBit.OTHER_READ -> S_IROTH.toUInt()
        TempfolderFileModeBit.OTHER_WRITE -> S_IWOTH.toUInt()
        TempfolderFileModeBit.OTHER_EXECUTE -> S_IXOTH.toUInt()
    }
