/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.winapi.model

import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_HIDDEN
import platform.windows.FILE_ATTRIBUTE_READONLY
import platform.windows.FILE_ATTRIBUTE_REPARSE_POINT
import platform.windows.FILE_ATTRIBUTE_SYSTEM

internal value class FileAttributes(
    val mask: UInt,
) {
    val isSymlinkOrReparsePoint: Boolean
        get() = mask.toInt() and FILE_ATTRIBUTE_REPARSE_POINT == FILE_ATTRIBUTE_REPARSE_POINT

    val isDirectory: Boolean
        get() = mask.toInt() and FILE_ATTRIBUTE_DIRECTORY == FILE_ATTRIBUTE_DIRECTORY

    val isReadOnly: Boolean
        get() = mask.toInt() and FILE_ATTRIBUTE_READONLY == FILE_ATTRIBUTE_READONLY

    val isHidden: Boolean
        get() = mask.toInt() and FILE_ATTRIBUTE_HIDDEN == FILE_ATTRIBUTE_HIDDEN

    val isSystem: Boolean
        get() = mask.toInt() and FILE_ATTRIBUTE_SYSTEM == FILE_ATTRIBUTE_SYSTEM
}
