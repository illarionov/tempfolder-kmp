/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.wasip1.type

import at.released.tempfolder.wasip1.WasiU64

/**
 * WASI Preview1 type `$dircookie`
 *
 * A reference to the offset of a directory entry.  The value 0 signifies the start of the directory.
 *
 * Definition:
 * ```
 * (typename $dircookie u64)
 * ```
 */
internal typealias Dircookie = ULong

internal typealias DircookieWasiType = WasiU64
