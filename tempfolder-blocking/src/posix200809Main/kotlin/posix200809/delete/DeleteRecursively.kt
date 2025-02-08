/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.DeleteRecursivelyException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.path.toPosixPathString
import at.released.tempfolder.posix200809.NativeDirent
import at.released.tempfolder.posix200809.OpenDirectoryAt
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType.DIRECTORY
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType.OTHER
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType.UNKNOWN
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem.EndOfStream
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem.Entry
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem.Error
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.isDirectory
import at.released.tempfolder.posix200809.nativeOpenDirectoryAt
import at.released.tempfolder.posix200809.platformDirent
import at.released.tempfolder.posix200809.toDirectoryStreamOrClose
import at.released.tempfolder.posix200809.unlinkDirectory
import at.released.tempfolder.posix200809.unlinkFile
import at.released.tempfolder.util.runStackSuppressedExceptions
import kotlinx.io.bytestring.ByteString
import platform.posix.EISDIR
import platform.posix.ENOENT
import platform.posix.EPERM
import platform.posix.dup
import platform.posix.errno

@Throws(DeleteRecursivelyException::class)
internal fun deleteRecursively(
    root: TempfolderPosixFileDescriptor,
    maxFileDescriptors: Int = 64,
) {
    BottomUpFileTreeRemover(
        root = root,
        openDirectoryAt = ::nativeOpenDirectoryAt,
        dirent = platformDirent,
        maxFileDescriptors = maxFileDescriptors,
    ).delete()
}

@Suppress("TooManyFunctions")
private class BottomUpFileTreeRemover<D>(
    private val root: TempfolderPosixFileDescriptor,
    private val openDirectoryAt: OpenDirectoryAt,
    private val dirent: NativeDirent<D>,
    private val maxFileDescriptors: Int = 64,
    private val maxSuppressedExceptions: Int = 8,
) {
    private val suppressedExceptions: MutableList<Exception> = ArrayList(maxSuppressedExceptions)
    private val stack: ArrayDeque<DirStream> = ArrayDeque(maxFileDescriptors)
    private var usedFds: Int = 0
    private val stream: DirStream get() = stack.last()

    @Throws(TempfolderIOException::class)
    fun delete() {
        val dup = dup(root.fd)
        if (dup == -1) {
            throw TempfolderNativeIOException(errno, "Can not duplicate descriptor. ${errnoDescription()}`")
        }
        val dir = dirent.toDirectoryStreamOrClose(dup)
        stack.addLast(PosixDirStream(dirent, dir))
        usedFds += 1

        runStackSuppressedExceptions(
            block = { deleteUnsafe() },
            finally = { close() },
        )
    }

    @Throws(TempfolderIOException::class)
    private fun deleteUnsafe() {
        while (stack.isNotEmpty()) {
            val stream = stack.last()
            when (val dirEntry = stream.readNext()) {
                is Entry -> handleEntry(dirEntry)
                is Error -> throw DeleteRecursivelyException(dirEntry.error).withSuppressedExceptions()
                EndOfStream -> {
                    val unlinkError = unlinkDirectory(stream.dirfd, PATH_CURRENT_DIRECTORY)
                    if (unlinkError != 0 && unlinkError != ENOENT) {
                        addSuppressedNativeIOException("Can not remove directory")
                    }
                    stack.removeLast()
                    usedFds -= 1
                    try {
                        stream.close()
                    } catch (ie: TempfolderIOException) {
                        throw DeleteRecursivelyException(ie).withSuppressedExceptions()
                    }
                }
            }
        }
    }

    private fun handleEntry(
        entry: Entry,
    ) {
        if (entry.name.isSpecialDirectory()) {
            return
        }
        when (entry.type) {
            OTHER -> handleNotDirectory(entry.name)
            UNKNOWN -> handleUnknown(entry.name)
            DIRECTORY -> handleDirectory(entry.name)
        }
    }

    private fun handleNotDirectory(name: PosixPathString) {
        val errno = unlinkFile(stream.dirfd, name)
        if (errno != 0) {
            addSuppressedNativeIOException("Can not remove file `${name.asStringOrDescription()}`")
        }
    }

    private fun handleUnknown(name: PosixPathString) {
        val errno = unlinkFile(stream.dirfd, name)
        when (errno) {
            0 -> Unit
            ENOENT -> Unit // Ignore
            EISDIR -> handleDirectory(name) // EISDIR is Linux-specific
            EPERM -> handleFileIfDirectory(name)
            else -> addSuppressedNativeIOException("Can not remove `${name.asStringOrDescription()}`")
        }
    }

    private fun handleFileIfDirectory(
        name: PosixPathString,
    ) {
        val isDirectory = try {
            isDirectory(stream.dirfd, name)
        } catch (isDirectoryException: TempfolderNativeIOException) {
            addSuppressedException(isDirectoryException)
            return
        }

        if (isDirectory) {
            handleDirectory(name)
        } else {
            addSuppressedNativeIOException("Can not remove `${name.asStringOrDescription()}`", EPERM)
        }
    }

    private fun handleDirectory(name: PosixPathString) {
        try {
            val dir = openDirectory(stream.dirfd, name)
            stack.addLast(PosixDirStream(dirent, dir))
            usedFds += 1
        } catch (error: TempfolderNativeIOException) {
            addSuppressedException(error)
            unlinkDirectory(stream.dirfd, name) // ignore errors
        }
    }

    @Throws(TempfolderNativeIOException::class)
    private fun openDirectory(
        dirrectory: TempfolderPosixFileDescriptor,
        path: PosixPathString,
    ): D {
        val fd = openDirectoryAt(dirrectory, path, true)
        if (fd.fd == -1) {
            throw TempfolderNativeIOException(errno, "Can not open directory `$path`. ${errnoDescription()}`")
        }
        return dirent.toDirectoryStreamOrClose(fd.fd)
    }

    private inline fun addSuppressedNativeIOException(errorText: String, error: Int = errno) {
        if (suppressedExceptions.size < maxSuppressedExceptions) {
            suppressedExceptions += TempfolderNativeIOException(
                error,
                "$errorText. ${errnoDescription(error)}",
            )
        }
    }

    private fun addSuppressedException(exception: Exception) {
        if (suppressedExceptions.size < maxSuppressedExceptions) {
            suppressedExceptions += exception
        }
    }

    private fun close() {
        val exceptions = stack.mapNotNull {
            try {
                it.close()
                null
            } catch (closeDirException: TempfolderNativeIOException) {
                closeDirException
            }
        }
        if (exceptions.isNotEmpty()) {
            val ex = TempfolderIOException("Can not close directories")
            exceptions.take(3).forEach { ex.addSuppressed(it) }
            throw ex
        }
    }

    private fun Throwable.withSuppressedExceptions(): Throwable = apply {
        suppressedExceptions.forEach { addSuppressed(it) }
    }

    private companion object {
        private val PATH_CURRENT_DIRECTORY = ".".toPosixPathString()
        private val PATH_PARENT_DIRECTORY = "..".toPosixPathString()

        private fun PosixPathString.isSpecialDirectory(): Boolean {
            return this.bytes.let { bytes: ByteString ->
                bytes == PATH_CURRENT_DIRECTORY.bytes || bytes == PATH_PARENT_DIRECTORY.bytes
            }
        }
    }
}
