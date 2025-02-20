/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.posix200809.TempDirectoryNativeIOException
import at.released.tempfolder.posix200809.errnoDescription

internal class SuppressedExceptionCollector(
    private val maxSuppressedExceptions: Int = 8,
) {
    private val exceptions: MutableList<TempDirectoryException> = ArrayList(maxSuppressedExceptions)

    inline fun addOrThrowNativeIOException(
        errorText: String,
        filePath: PosixPath,
        errno: Int = platform.posix.errno,
        parent: TempDirectoryIOException? = null,
    ) {
        val exception = TempDirectoryNativeIOException(
            errno,
            "$errorText `${filePath.asStringOrDescription()}`. ${errnoDescription(errno)}",
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
