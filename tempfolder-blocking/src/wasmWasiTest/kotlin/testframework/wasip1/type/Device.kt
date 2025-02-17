/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.wasip1.type

/**
 * WASI Preview1 type `$device`
 *
 * Identifier for a device containing a file system. Can be used in combination with `inode` to uniquely identify
 * a file or directory in the filesystem.
 *
 * Definition:
 * ```
 * (typename $device u64)
 * ```
 */
internal typealias Device = ULong
