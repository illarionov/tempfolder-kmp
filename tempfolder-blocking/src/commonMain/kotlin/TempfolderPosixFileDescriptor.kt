/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

import kotlin.jvm.JvmInline

@JvmInline
public value class TempfolderPosixFileDescriptor(
    public val fd: Int,
)
