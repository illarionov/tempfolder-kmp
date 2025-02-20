/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.dsl.TempfolderFileModeBit
import java.nio.file.LinkOption
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE
import java.nio.file.attribute.PosixFilePermission.GROUP_READ
import java.nio.file.attribute.PosixFilePermission.GROUP_WRITE
import java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE
import java.nio.file.attribute.PosixFilePermission.OTHERS_READ
import java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE
import java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import java.nio.file.attribute.PosixFilePermission.OWNER_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_WRITE

private val PosixFilePermission.tempfolderBit: TempfolderFileModeBit
    get() = when (this) {
        OWNER_READ -> TempfolderFileModeBit.USER_READ
        OWNER_WRITE -> TempfolderFileModeBit.USER_WRITE
        OWNER_EXECUTE -> TempfolderFileModeBit.USER_EXECUTE
        GROUP_READ -> TempfolderFileModeBit.GROUP_READ
        GROUP_WRITE -> TempfolderFileModeBit.GROUP_WRITE
        GROUP_EXECUTE -> TempfolderFileModeBit.GROUP_EXECUTE
        OTHERS_READ -> TempfolderFileModeBit.OTHER_READ
        OTHERS_WRITE -> TempfolderFileModeBit.OTHER_WRITE
        OTHERS_EXECUTE -> TempfolderFileModeBit.OTHER_EXECUTE
    }

internal fun linkOptions(followSymlinks: Boolean): Array<LinkOption> = if (followSymlinks) {
    emptyArray()
} else {
    arrayOf(LinkOption.NOFOLLOW_LINKS)
}

internal fun Set<PosixFilePermission>.toTempfolderPermissions(): Set<TempfolderFileModeBit> {
    return this.mapTo(mutableSetOf()) { it.tempfolderBit }
}

internal fun Set<TempfolderFileModeBit>.toNioPosixPermissions(): Set<PosixFilePermission> {
    return PosixFilePermission.entries.mapNotNullTo(mutableSetOf()) { posixBit ->
        posixBit.takeIf { it.tempfolderBit in this }
    }
}
