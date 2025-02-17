/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.wasip1.type

import at.released.tempfolder.wasip1.type.Size

/**
 * WASI Preview1 type `$ciovec`
 *
 * A region of memory for scatter/gather writes.
 *
 * @param buf The address of the buffer to be written.
 * @param bufLen The length of the buffer to be written.
 *
 * Definition:
 * ```
 * (typename $ciovec
 *   (record
 *     ;;; The address of the buffer to be written.
 *     (field $buf (@witx const_pointer u8))
 *     ;;; The length of the buffer to be written.
 *     (field $buf_len $size)
 *   )
 * )
 * ```
 */
public data class Ciovec(
    public val buf: Int,
    public val bufLen: Size,
)
