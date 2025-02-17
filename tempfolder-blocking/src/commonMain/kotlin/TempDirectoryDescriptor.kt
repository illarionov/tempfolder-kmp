/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

import kotlin.jvm.JvmInline

internal fun Int.asFileDescriptor(): TempDirectoryDescriptor = TempDirectoryDescriptor(this)

@JvmInline
public value class TempDirectoryDescriptor(
    public val fd: Int,
) {
    public companion object {
        internal val CURRENT_WORKING_DIRECTORY = TempDirectoryDescriptor(-100)
    }
}
