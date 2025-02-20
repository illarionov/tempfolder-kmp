/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.wasip1.type

/**
 * WASI Preview1 type `$inode`
 *
 * File serial number that is unique within its file system.
 *
 * Definition:
 * ```
 * (typename $inode u64)
 * ```
 */
internal typealias Inode = ULong
