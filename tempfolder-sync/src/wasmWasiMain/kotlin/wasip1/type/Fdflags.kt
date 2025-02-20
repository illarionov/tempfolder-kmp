/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MaxLineLength")

package at.released.tempfolder.wasip1.type

import at.released.tempfolder.wasip1.WasiU16

/**
 * WASI Preview1 type `$fdflags`
 *
 * File descriptor flags.
 *
 * Representation: `u16`
 *
 * Definition:
 * ```
 * (typename $fdflags
 *   (flags (@witx repr u16)
 *     ;;; Append mode: Data written to the file is always appended to the file's end.
 *     $append
 *     ;;; Write according to synchronized I/O data integrity completion. Only the data stored in the file is synchronized.
 *     $dsync
 *     ;;; Non-blocking mode.
 *     $nonblock
 *     ;;; Synchronized read I/O operations.
 *     $rsync
 *     ;;; Write according to synchronized I/O file integrity completion. In
 *     ;;; addition to synchronizing the data stored in the file, the implementation
 *     ;;; may also synchronously update the file's metadata.
 *     $sync
 *   )
 * )
 * ```
 */
internal typealias Fdflags = UShort

internal typealias FdflagsWasmType = WasiU16

internal object FdflagsFlag {
    /**
     * Append mode: Data written to the file is always appended to the file's end.
     */
    val APPEND: UShort = 0x01.toUShort()

    /**
     * Write according to synchronized I/O data integrity completion. Only the data stored in the file is synchronized.
     */
    val DSYNC: UShort = 0x02.toUShort()

    /**
     * Non-blocking mode.
     */
    val NONBLOCK: UShort = 0x04.toUShort()

    /**
     * Synchronized read I/O operations.
     */
    val RSYNC: UShort = 0x08.toUShort()

    /**
     * Write according to synchronized I/O file integrity completion. In addition to synchronizing the data stored in the file, the implementation may also synchronously update the file's metadata.
     */
    val SYNC: UShort = 0x10.toUShort()
}
