/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.platform.androidnative.unlinkat
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import platform.posix.AT_REMOVEDIR

internal actual fun platformUnlinkat(dirfd: Int, path: CPointer<ByteVar>, removeDirectory: Boolean): Int {
    return unlinkat(dirfd, path, if (removeDirectory) AT_REMOVEDIR else 0)
}
