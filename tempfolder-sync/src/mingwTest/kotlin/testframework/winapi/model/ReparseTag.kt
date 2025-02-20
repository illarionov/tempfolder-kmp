/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.winapi.model

import platform.windows.IO_REPARSE_TAG_SYMLINK

internal value class ReparseTag(
    val code: UInt,
) {
    val isSymlink: Boolean get() = code == IO_REPARSE_TAG_SYMLINK
}
