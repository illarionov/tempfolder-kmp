/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.get
import platform.windows.WCHARVar

// XXX Consider if we need a more sophisticated implementation.
internal fun windowsAppendPath(root: String, appendPath: String): String {
    if (root.isEmpty()) {
        return appendPath
    }

    val firstNonSeparator = appendPath.indices.firstOrNull {
        !appendPath[it].isWindowsPathSeparator()
    } ?: return root

    return buildString {
        append(root)
        if (!last().isWindowsPathSeparator()) {
            append('\\')
        }
        append(appendPath, firstNonSeparator, appendPath.length)
    }
}

internal fun windowsPathEndWithSpecialDirectory(path: String): Boolean {
    val pathTrimmed = path.trimEnd('\\', '/', ' ')
    return pathTrimmed.endsWith(".") || pathTrimmed.endsWith("..")
}

private fun Char.isWindowsPathSeparator(): Boolean = this == '\\' || this == '/'

/**
 * @return an array of UCS-2 characters read from the given pointer with the specified [length].
 * Note: The returned array may not form a well-formed UTF-16-encoded string.
 */
internal fun CArrayPointer<WCHARVar>.readChars(length: Int): CharArray {
    require(length >= 0)
    return CharArray(length) { index -> this[index].toInt().toChar() }
}
