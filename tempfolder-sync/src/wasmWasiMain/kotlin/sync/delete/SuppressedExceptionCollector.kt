/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.delete

import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.TempDirectoryWasiIOException
import at.released.tempfolder.path.TempDirectoryPath.MultibytePath
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.wasip1.type.Errno

internal class SuppressedExceptionCollector(
    private val maxSuppressedExceptions: Int = 8,
) {
    private val exceptions: MutableList<TempDirectoryException> = ArrayList(maxSuppressedExceptions)

    @Suppress("NOTHING_TO_INLINE")
    inline fun addOrThrowNativeIOException(
        errorText: String,
        filePath: MultibytePath,
        errno: Int,
        parent: TempDirectoryIOException? = null,
    ) {
        val exception = TempDirectoryWasiIOException(
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

    fun removeFirstOrNull(): TempDirectoryException? = exceptions.removeFirstOrNull()

    fun addSuppressedToThrowable(throwable: Throwable) {
        exceptions.forEach { throwable.addSuppressed(it) }
    }
}
