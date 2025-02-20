/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.wasip1.type

import at.released.tempfolder.wasip1.WasiU16

/**
 * WASI Preview1 type `$oflags`
 *
 * Open flags used by `path_open`.
 *
 * Representation: `u16`
 *
 * Definition:
 * ```
 * (typename $oflags
 *   (flags (@witx repr u16)
 *     ;;; Create file if it does not exist.
 *     $creat
 *     ;;; Fail if not a directory.
 *     $directory
 *     ;;; Fail if file already exists.
 *     $excl
 *     ;;; Truncate file to size 0.
 *     $trunc
 *   )
 * )
 * ```
 */
internal typealias Oflags = UShort

internal typealias OflagsWasmType = WasiU16

internal object OflagsFlag {
    /**
     * Create file if it does not exist.
     */
    val CREAT: UShort = 0x01.toUShort()

    /**
     * Fail if not a directory.
     */
    val DIRECTORY: UShort = 0x02.toUShort()

    /**
     * Fail if file already exists.
     */
    val EXCL: UShort = 0x04.toUShort()

    /**
     * Truncate file to size 0.
     */
    val TRUNC: UShort = 0x08.toUShort()
}
