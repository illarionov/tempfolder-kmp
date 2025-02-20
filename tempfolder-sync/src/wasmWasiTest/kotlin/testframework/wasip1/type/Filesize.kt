/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.wasip1.type

/**
 * WASI Preview1 type `$filesize`
 *
 * Non-negative file size or length of a region within a file.
 *
 * Definition:
 * ```
 * (typename $filesize u64)
 * ```
 */
internal typealias Filesize = ULong
