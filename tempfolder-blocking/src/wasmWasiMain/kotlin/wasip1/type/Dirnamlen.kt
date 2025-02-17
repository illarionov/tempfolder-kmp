/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.wasip1.type

/**
 * WASI Preview1 type `$dirnamlen`
 *
 * The type for the `dirent::d_namlen` field of `dirent` struct.
 *
 * Definition:
 * ```
 * (typename $dirnamlen u32)
 * ```
 */
internal typealias Dirnamlen = UInt
