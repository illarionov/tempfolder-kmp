/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.DeleteRecursivelyException
import at.released.tempfolder.TempfolderException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.PATH_CURRENT_DIRECTORY
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.PosixPathStringComponent
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.path.isSpecialDirectory
import at.released.tempfolder.posix200809.NativeDirent
import at.released.tempfolder.posix200809.OpenDirectoryAt
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType.DIRECTORY
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType.OTHER
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType.UNKNOWN
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem.EndOfStream
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem.Entry
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem.Error
import at.released.tempfolder.posix200809.errnoDescription
import at.released.tempfolder.posix200809.isDirectory
import at.released.tempfolder.posix200809.nativeOpenDirectoryAt
import at.released.tempfolder.posix200809.openDirectoryStreamOrCloseFd
import at.released.tempfolder.posix200809.platformDirent
import at.released.tempfolder.posix200809.unlinkDirectory
import at.released.tempfolder.posix200809.unlinkFile
import at.released.tempfolder.util.runBlockStackSuppressedExceptions
import platform.posix.EISDIR
import platform.posix.ENOENT
import platform.posix.EPERM
import platform.posix.errno

@Throws(DeleteRecursivelyException::class)
internal fun deleteRecursively(
    root: TempfolderPosixFileDescriptor,
    maxFileDescriptors: Int = 64,
) {
    BottomUpFileTreeRemover(
        root = root,
        openDirectoryAt = ::nativeOpenDirectoryAt,
        direntApi = platformDirent,
        maxFileDescriptors = maxFileDescriptors,
    ).delete()
}

@Suppress("TooManyFunctions")
private class BottomUpFileTreeRemover<D>(
    private val root: TempfolderPosixFileDescriptor,
    private val openDirectoryAt: OpenDirectoryAt,
    private val direntApi: NativeDirent<D>,
    maxFileDescriptors: Int = 64,
    private val maxSuppressedExceptions: Int = 8,
) {
    private val suppressedUnlinkExceptions: MutableList<Exception> = ArrayList(maxSuppressedExceptions)
    private val pathDequeue = PathDequeue(direntApi, maxFileDescriptors)

    @Throws(TempfolderIOException::class)
    fun delete() {
        enterDirectoryOrThrow(root, PATH_CURRENT_DIRECTORY, PATH_CURRENT_DIRECTORY)
        runBlockStackSuppressedExceptions(
            block = {
                try {
                    deleteUnsafe()
                } catch (te: TempfolderException) {
                    throw te.addSuppressedExceptions()
                }
            },
            finally = { close() },
        )
    }

    @Throws(TempfolderIOException::class)
    private fun deleteUnsafe() {
        while (pathDequeue.isNotEmpty()) {
            val stream = pathDequeue.last()
            when (val dirEntry = stream.readNext()) {
                is Error -> throw DeleteRecursivelyException(dirEntry.error)
                is Entry -> handleEntry(dirEntry.name, dirEntry.type)
                EndOfStream -> handleEndOfStream()
            }
        }
    }

    private fun handleEntry(
        basename: PosixPathStringComponent,
        type: DirEntryType,
    ) {
        if (basename.isSpecialDirectory()) {
            return
        }

        @Suppress("UNCHECKED_CAST")
        when (val stream = pathDequeue.last()) {
            is OpenDirStream<*> -> (stream as OpenDirStream<D>).handleOpenDirStreamEntry(basename, type) {
                stream.enterDirectory(it)
            }

            is PreloadedDirStream -> {
                check(type == DIRECTORY) { "Unexpected entry type" }
                val path = pathDequeue.getPathFromRoot(basename)
                try {
                    enterDirectoryOrThrow(root, path, basename)
                } catch (error: TempfolderNativeIOException) {
                    addSuppressedException(error)
                    unlinkDirectory(root, path) // ignore errors
                }
            }
        }
    }

    private fun handleEndOfStream() {
        val dirStream = pathDequeue.last()
        val (rootFd, path) = when (dirStream) {
            is OpenDirStream<*> -> dirStream.dirfd to PATH_CURRENT_DIRECTORY
            is PreloadedDirStream -> root to pathDequeue.getPathFromRoot(PATH_CURRENT_DIRECTORY)
        }
        val unlinkError = unlinkDirectory(rootFd, path)
        if (unlinkError != 0 && unlinkError != ENOENT) {
            addSuppressedNativeIOException("Can not remove directory")
        }
        pathDequeue.removeLast()
        try {
            dirStream.close()
        } catch (ie: TempfolderIOException) {
            throw DeleteRecursivelyException(ie)
        }
    }

    private fun OpenDirStream<D>.enterDirectory(
        name: PosixPathStringComponent,
        tryUnlinkOnError: Boolean = true,
    ) {
        try {
            enterDirectoryOrThrow(dirfd, name, name)
        } catch (error: TempfolderNativeIOException) {
            addSuppressedException(error)
            if (tryUnlinkOnError) {
                unlinkDirectory(dirfd, name) // ignore errors
            }
        }
    }

    @Throws(TempfolderNativeIOException::class)
    private fun enterDirectoryOrThrow(
        dirFd: TempfolderPosixFileDescriptor,
        pathFromDirFd: PosixPathString,
        basename: PosixPathStringComponent,
    ) {
        pathDequeue.reserveFileDescriptor(::preloadDirectory)
        val fd = openDirectoryAt(dirFd, pathFromDirFd, true)
        if (fd.fd == -1) {
            // TODO absolute path
            throw TempfolderNativeIOException(errno, "Can not open directory")
        }
        val dir = direntApi.openDirectoryStreamOrCloseFd(fd.fd)
        pathDequeue.addLast(dir, fd, basename)
    }

    @Throws(TempfolderNativeIOException::class)
    private fun preloadDirectory(
        stream: OpenDirStream<D>,
    ): List<Entry> {
        val entries: MutableList<Entry> = mutableListOf()
        while (true) {
            when (val entry = stream.readNext()) {
                is Entry -> stream.handleOpenDirStreamEntry(entry.name, entry.type) {
                    entries.add(entry)
                }

                is Error -> throw TempfolderIOException(entry.error)
                EndOfStream -> break
            }
        }
        return entries
    }

    private inline fun OpenDirStream<D>.handleOpenDirStreamEntry(
        name: PosixPathStringComponent,
        type: DirEntryType,
        crossinline onDirectory: (PosixPathStringComponent) -> Unit,
    ) {
        when (type) {
            DIRECTORY -> onDirectory(name)
            OTHER -> {
                val errno = unlinkFile(dirfd, name)
                if (errno != 0) {
                    addSuppressedNativeIOException("Can not remove file `${name.asStringOrDescription()}`")
                }
            }

            UNKNOWN -> {
                val errno = unlinkFile(dirfd, name)
                // EISDIR is Linux-specific
                when (errno) {
                    0 -> Unit
                    ENOENT -> Unit // Ignore
                    EISDIR -> onDirectory(name)
                    EPERM -> try {
                        val isDirectory = isDirectory(dirfd, name)
                        if (isDirectory) {
                            onDirectory(name)
                        } else {
                            addSuppressedNativeIOException("Can not remove `${name.asStringOrDescription()}`", EPERM)
                        }
                    } catch (isDirectoryException: TempfolderNativeIOException) {
                        addSuppressedException(isDirectoryException)
                        unlinkDirectory(dirfd, name) // Try to unlink directory, ignore errors
                    }

                    else -> addSuppressedNativeIOException(
                        "Can not remove `${basename.asStringOrDescription()}`",
                    )
                }
            }
        }
    }

    private inline fun addSuppressedNativeIOException(errorText: String, error: Int = errno) {
        if (suppressedUnlinkExceptions.size < maxSuppressedExceptions) {
            suppressedUnlinkExceptions += TempfolderNativeIOException(
                error,
                "$errorText. ${errnoDescription(error)}",
            )
        }
    }

    private fun addSuppressedException(exception: Exception) {
        if (suppressedUnlinkExceptions.size < maxSuppressedExceptions) {
            suppressedUnlinkExceptions += exception
        }
    }

    private fun close() {
        pathDequeue.close()
    }

    private fun Throwable.addSuppressedExceptions(): Throwable = apply {
        suppressedExceptions.forEach { addSuppressed(it) }
    }
}
