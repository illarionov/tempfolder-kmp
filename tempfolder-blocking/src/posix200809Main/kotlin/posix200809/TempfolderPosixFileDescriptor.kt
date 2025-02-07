/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

internal fun Int.asFileDescriptor(): TempfolderPosixFileDescriptor = TempfolderPosixFileDescriptor(this)

public value class TempfolderPosixFileDescriptor(
    public val fd: Int,
)
