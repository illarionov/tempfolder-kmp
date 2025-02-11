/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.TempfolderException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.errnoDescription

internal class SuppressedExceptionCollector(
    private val maxSuppressedExceptions: Int = 8,
) {
    private val exceptions: MutableList<TempfolderException> = ArrayList(maxSuppressedExceptions)

    inline fun addNativeIOException(
        errorText: String,
        filePath: PosixPathString,
        errno: Int = platform.posix.errno,
        parent: TempfolderIOException? = null,
    ) {
        if (exceptions.size < maxSuppressedExceptions) {
            exceptions += TempfolderNativeIOException(
                errno,
                "$errorText `${filePath.asStringOrDescription()}`. ${errnoDescription(errno)}",
                parent,
            )
        }
    }

    fun removeFirstOrNull(): TempfolderException? = exceptions.removeFirstOrNull()

    fun addSuppressedToThrowable(throwable: Throwable) {
        exceptions.forEach { throwable.addSuppressed(it) }
    }
}
