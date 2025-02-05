/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.nativefunc

import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.strerror

internal fun errnoDescription(errno: Int = platform.posix.errno) =
    "Error $errno (${strerror(errno)?.toKStringFromUtf8()})`"
