/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.wasip1

import at.released.tempfolder.testframework.wasip1.type.Ciovec
import kotlin.wasm.unsafe.Pointer

internal const val IOVEC_PACKED_SIZE = 8

internal fun Ciovec.writeTo(pointer: Pointer) {
    pointer.storeInt(this.buf.toInt())
    (pointer + 4).storeInt(this.bufLen.toInt())
}
