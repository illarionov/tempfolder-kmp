/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.path.PosixPath

internal expect fun platformMkdirat(
    base: TempDirectoryDescriptor,
    directoryName: PosixPath,
    mode: UInt,
): Int
