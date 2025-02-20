/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.wasip1.type

import at.released.tempfolder.wasip1.WasiU32

/**
 * WASI Preview1 type `$size`
 *
 * Definition:
 * ```
 * (typename $size u32)
 * ```
 */
internal typealias Size = UInt

internal typealias SizeWasiType = WasiU32
