/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.dsl.TempDirectoryFileModeBit
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

private val PosixFilePermission.tempfolderBit: TempDirectoryFileModeBit
    get() = when (this) {
        OWNER_READ -> TempDirectoryFileModeBit.USER_READ
        OWNER_WRITE -> TempDirectoryFileModeBit.USER_WRITE
        OWNER_EXECUTE -> TempDirectoryFileModeBit.USER_EXECUTE
        GROUP_READ -> TempDirectoryFileModeBit.GROUP_READ
        GROUP_WRITE -> TempDirectoryFileModeBit.GROUP_WRITE
        GROUP_EXECUTE -> TempDirectoryFileModeBit.GROUP_EXECUTE
        OTHERS_READ -> TempDirectoryFileModeBit.OTHER_READ
        OTHERS_WRITE -> TempDirectoryFileModeBit.OTHER_WRITE
        OTHERS_EXECUTE -> TempDirectoryFileModeBit.OTHER_EXECUTE
    }

internal fun linkOptions(followSymlinks: Boolean): Array<LinkOption> = if (followSymlinks) {
    emptyArray()
} else {
    arrayOf(LinkOption.NOFOLLOW_LINKS)
}

internal fun Set<PosixFilePermission>.toTempfolderPermissions(): Set<TempDirectoryFileModeBit> {
    return this.mapTo(mutableSetOf()) { it.tempfolderBit }
}

internal fun Set<TempDirectoryFileModeBit>.toNioPosixPermissions(): Set<PosixFilePermission> {
    return PosixFilePermission.entries.mapNotNullTo(mutableSetOf()) { posixBit ->
        posixBit.takeIf { it.tempfolderBit in this }
    }
}
