/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.dsl

import platform.posix.S_IRGRP
import platform.posix.S_IROTH
import platform.posix.S_IRUSR
import platform.posix.S_IWGRP
import platform.posix.S_IWOTH
import platform.posix.S_IWUSR
import platform.posix.S_IXGRP
import platform.posix.S_IXOTH
import platform.posix.S_IXUSR
import platform.posix.mode_t

internal fun Set<TempfolderFileModeBit>.toPosixMode(): mode_t = this.fold(0U) { mask, bit -> mask or bit.posixMask }

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
