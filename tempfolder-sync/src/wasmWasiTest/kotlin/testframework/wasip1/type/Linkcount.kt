/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.wasip1.type

/**
 * WASI Preview1 type `$linkcount`
 *
 * Number of hard links to an inode.
 *
 * Definition:
 * ```
 * (typename $linkcount u64)
 * ```
 */
internal typealias Linkcount = ULong
