/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.platform.apple.realpath
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer

internal actual fun platformRealpath(pathNative: CPointer<ByteVar>): CPointer<ByteVar>? = realpath(pathNative, null)
