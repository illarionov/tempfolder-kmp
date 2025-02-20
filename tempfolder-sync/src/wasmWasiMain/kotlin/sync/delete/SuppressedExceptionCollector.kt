/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.delete

import at.released.tempfolder.TempfolderException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderWasiIOException
import at.released.tempfolder.path.TempfolderPathString.MultibytePathString
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.wasip1.type.Errno

internal class SuppressedExceptionCollector(
    private val maxSuppressedExceptions: Int = 8,
) {
    private val exceptions: MutableList<TempfolderException> = ArrayList(maxSuppressedExceptions)

    @Suppress("NOTHING_TO_INLINE")
    inline fun addOrThrowNativeIOException(
        errorText: String,
        filePath: MultibytePathString,
        errno: Int,
        parent: TempfolderIOException? = null,
    ) {
        val exception = TempfolderWasiIOException(
            errno,
            "$errorText `${filePath.asStringOrDescription()}`. ${Errno.nameFromCode(errno)}",
            parent,
        )
        if (exceptions.size < maxSuppressedExceptions) {
            exceptions += exception
        } else {
            throw exception
        }
    }

    fun removeFirstOrNull(): TempfolderException? = exceptions.removeFirstOrNull()

    fun addSuppressedToThrowable(throwable: Throwable) {
        exceptions.forEach { throwable.addSuppressed(it) }
    }
}
