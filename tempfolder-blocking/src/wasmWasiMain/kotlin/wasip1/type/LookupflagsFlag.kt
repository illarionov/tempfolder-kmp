/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.wasip1.type

import at.released.tempfolder.wasip1.WasiU32

/**
 * WASI Preview1 type `$lookupflags`
 *
 * Flags determining the method of how paths are resolved.
 *
 * Representation: `u32`
 *
 * Definition:
 * ```
 * (typename $lookupflags
 *   (flags (@witx repr u32)
 *     ;;; As long as the resolved path corresponds to a symbolic link, it is expanded.
 *     $symlink_follow
 *   )
 * )
 * ```
 */
internal typealias Lookupflags = UInt

internal typealias LookupflagsWasmType = WasiU32

internal object LookupflagsFlag {
    /**
     * As long as the resolved path corresponds to a symbolic link, it is expanded.
     */
    public const val SYMLINK_FOLLOW: UInt = 0x01U
}
