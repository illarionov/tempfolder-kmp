/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.dsl.TempDirectoryFileModeBit
import platform.posix.S_IRGRP
import platform.posix.S_IROTH
import platform.posix.S_IRUSR
import platform.posix.S_IWGRP
import platform.posix.S_IWOTH
import platform.posix.S_IWUSR
import platform.posix.S_IXGRP
import platform.posix.S_IXOTH
import platform.posix.S_IXUSR

internal fun Set<TempDirectoryFileModeBit>.toPosixMode(): UInt = this.fold(0U) { mask, bit -> mask or bit.posixMask }

internal fun TempDirectoryFileModeBit.Companion.fromPosixMode(mode: UInt): Set<TempDirectoryFileModeBit> {
    return TempDirectoryFileModeBit.entries.mapNotNullTo(mutableSetOf()) {
        if (mode and it.posixMask == it.posixMask) it else null
    }
}

private val TempDirectoryFileModeBit.posixMask: UInt
    get() = when (this) {
        TempDirectoryFileModeBit.SUID -> 0b100_000_000_000U
        TempDirectoryFileModeBit.SGID -> 0b010_000_000_000U
        TempDirectoryFileModeBit.STICKY -> 0b001_000_000_000U
        TempDirectoryFileModeBit.USER_READ -> S_IRUSR.toUInt()
        TempDirectoryFileModeBit.USER_WRITE -> S_IWUSR.toUInt()
        TempDirectoryFileModeBit.USER_EXECUTE -> S_IXUSR.toUInt()
        TempDirectoryFileModeBit.GROUP_READ -> S_IRGRP.toUInt()
        TempDirectoryFileModeBit.GROUP_WRITE -> S_IWGRP.toUInt()
        TempDirectoryFileModeBit.GROUP_EXECUTE -> S_IXGRP.toUInt()
        TempDirectoryFileModeBit.OTHER_READ -> S_IROTH.toUInt()
        TempDirectoryFileModeBit.OTHER_WRITE -> S_IWOTH.toUInt()
        TempDirectoryFileModeBit.OTHER_EXECUTE -> S_IXOTH.toUInt()
    }
