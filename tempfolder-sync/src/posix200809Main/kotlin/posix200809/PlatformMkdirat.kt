/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.path.PosixPathString

internal expect fun platformMkdirat(
    base: TempDirectoryDescriptor,
    directoryName: PosixPathString,
    mode: UInt,
): Int
