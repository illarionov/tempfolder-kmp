/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.TempDirectoryDeleteException
import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.PATH_CURRENT_DIRECTORY
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.path.isSpecialDirectory
import at.released.tempfolder.posix200809.OpenDirectoryAt
import at.released.tempfolder.posix200809.PlatformDirent
import at.released.tempfolder.posix200809.TempDirectoryNativeIOException
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType.DIRECTORY
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType.OTHER
import at.released.tempfolder.posix200809.delete.DirStream.DirEntryType.UNKNOWN
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem.EndOfStream
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem.Entry
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem.Error
import at.released.tempfolder.posix200809.isDirectory
import at.released.tempfolder.posix200809.nativeOpenDirectoryAt
import at.released.tempfolder.posix200809.openDirectoryStreamOrCloseFd
import at.released.tempfolder.posix200809.platformDirent
import at.released.tempfolder.posix200809.platformUnlinkDirectory
import at.released.tempfolder.posix200809.platformUnlinkFile
import platform.posix.EISDIR
import platform.posix.ENOENT
import platform.posix.EPERM
import platform.posix.errno

/**
 * Recursively deletes the contents of the directory referenced by the descriptor [root].
 * Does not delete the directory itself. Does not close the file descriptor.
 *
 * @param maxFileDescriptors the maximum number of simultaneously open directories that can be used during deletion.
 */
@Throws(TempDirectoryDeleteException::class)
internal fun deleteRecursively(
    root: TempDirectoryDescriptor,
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
    private val root: TempDirectoryDescriptor,
    private val openDirectoryAt: OpenDirectoryAt,
    private val direntApi: PlatformDirent<D>,
    maxFileDescriptors: Int = 64,
    maxSuppressedExceptions: Int = 8,
) {
    private val suppressedExceptions = SuppressedExceptionCollector(maxSuppressedExceptions)
    private val pathDequeue = PathDequeue(direntApi, maxFileDescriptors)

    @Throws(TempDirectoryIOException::class)
    @Suppress("ThrowsCount")
    fun delete() {
        try {
            enterDirectoryOrThrow(root, PATH_CURRENT_DIRECTORY, PATH_CURRENT_DIRECTORY)
        } catch (tne: TempDirectoryNativeIOException) {
            throw TempDirectoryNativeIOException(tne.errno, "Can not open directory to remove", tne)
        }

        try {
            deleteUnsafe()
            suppressedExceptions.removeFirstOrNull()?.let {
                throw TempDirectoryDeleteException(
                    "${it.message}. Suppressed exceptions may contain other errors",
                    it,
                )
            }
        } catch (ie: TempDirectoryException) {
            suppressedExceptions.addSuppressedToThrowable(ie)
            throw ie
        } finally {
            closeSilent()
        }
    }

    private fun closeSilent() {
        try {
            close()
        } catch (@Suppress("SwallowedException") ex: TempDirectoryException) {
            // Ignore errors
        }
    }

    @Throws(TempDirectoryIOException::class)
    private fun deleteUnsafe() {
        while (pathDequeue.isNotEmpty()) {
            val stream = pathDequeue.last()
            when (val dirEntry = stream.readNext()) {
                is Entry -> handleEntry(dirEntry.name, dirEntry.type)
                is Error -> throw TempDirectoryIOException(
                    "Failed to read directory entry at ${pathDequeue.getPathFromRoot(stream).asStringOrDescription()}",
                    dirEntry.error,
                )

                EndOfStream -> handleEndOfStream()
            }
        }
    }

    @Throws(TempDirectoryIOException::class)
    private fun handleEndOfStream() {
        val closingStream = pathDequeue.removeLast()
        closingStream.close()
        if (pathDequeue.isNotEmpty()) {
            unlinkEmptyDirectory(closingStream.basename)
        }
    }

    @Throws(TempDirectoryIOException::class)
    private fun unlinkEmptyDirectory(name: PosixPath.Component) {
        val dir = pathDequeue.last()
        val (rootFd, path) = when (dir) {
            is OpenDirStream<*> -> dir.dirfd to name
            is PreloadedDirStream -> root to pathDequeue.getPathFromRoot(dir, name)
        }

        val unlinkError = platformUnlinkDirectory(rootFd, path)
        if (unlinkError != 0 && unlinkError != ENOENT) {
            suppressedExceptions.addOrThrowNativeIOException(
                errorText = "Can not remove directory",
                filePath = pathDequeue.getPathFromRoot(dir, name),
                errno = unlinkError,
            )
        }
    }

    private fun handleEntry(
        basename: PosixPath.Component,
        type: DirEntryType,
    ) {
        if (basename.isSpecialDirectory()) {
            return
        }

        @Suppress("UNCHECKED_CAST")
        when (val stream = pathDequeue.last()) {
            is OpenDirStream<*> -> handleOpenDirStreamEntry(stream as OpenDirStream<D>, basename, type) {
                try {
                    enterDirectoryOrThrow(stream.dirfd, basename, basename)
                } catch (tne: TempDirectoryNativeIOException) {
                    suppressedExceptions.addOrThrowNativeIOException(
                        errorText = "Can not enter directory",
                        filePath = pathDequeue.getPathFromRoot(stream, basename),
                        errno = tne.errno,
                        parent = tne,
                    )
                    platformUnlinkDirectory(stream.dirfd, basename) // ignore errors
                }
            }

            is PreloadedDirStream -> {
                check(type == DIRECTORY) { "Unexpected entry type" }
                val path = pathDequeue.getPathFromRoot(stream, basename)
                try {
                    enterDirectoryOrThrow(root, path, basename)
                } catch (error: TempDirectoryNativeIOException) {
                    suppressedExceptions.addOrThrowNativeIOException(
                        errorText = "Can not enter directory",
                        filePath = path,
                        errno = error.errno,
                        parent = error,
                    )
                    platformUnlinkDirectory(root, path) // ignore errors
                }
            }
        }
    }

    @Throws(TempDirectoryNativeIOException::class)
    private fun enterDirectoryOrThrow(
        dirFd: TempDirectoryDescriptor,
        pathFromDirFd: PosixPath,
        basename: PosixPath.Component,
    ) {
        pathDequeue.reserveFileDescriptor(::preloadDirectory)
        val fd = openDirectoryAt(dirFd, pathFromDirFd, true)
        if (fd.fd == -1) {
            throw TempDirectoryNativeIOException(errno, "Can not open directory")
        }
        val dir = direntApi.openDirectoryStreamOrCloseFd(fd)
        pathDequeue.addLast(dir, fd, basename)
    }

    @Throws(TempDirectoryNativeIOException::class)
    private fun preloadDirectory(
        stream: OpenDirStream<D>,
    ): List<Entry> {
        val directories: MutableList<Entry> = mutableListOf()
        while (true) {
            when (val entry = stream.readNext()) {
                is Entry -> handleOpenDirStreamEntry(stream, entry.name, entry.type) {
                    directories.add(entry)
                }

                is Error -> throw TempDirectoryIOException(entry.error)
                EndOfStream -> break
            }
        }
        return directories
    }

    private inline fun handleOpenDirStreamEntry(
        stream: OpenDirStream<D>,
        name: PosixPath.Component,
        type: DirEntryType,
        crossinline onDirectory: (PosixPath.Component) -> Unit,
    ) {
        when (type) {
            DIRECTORY -> onDirectory(name)
            OTHER -> {
                val errno = platformUnlinkFile(stream.dirfd, name)
                if (errno != 0) {
                    suppressedExceptions.addOrThrowNativeIOException(
                        errorText = "Failed to delete file",
                        filePath = pathDequeue.getPathFromRoot(stream, name),
                        errno = errno,
                    )
                }
            }

            UNKNOWN -> {
                // Try to delete as a file
                val errno = platformUnlinkFile(stream.dirfd, name)
                when (errno) {
                    0 -> Unit
                    ENOENT -> Unit // Ignore
                    EISDIR -> onDirectory(name) // EISDIR is Linux-specific
                    EPERM -> try {
                        if (isDirectory(stream.dirfd, name)) {
                            onDirectory(name)
                        } else {
                            suppressedExceptions.addOrThrowNativeIOException(
                                errorText = "Failed to delete file or directory",
                                filePath = pathDequeue.getPathFromRoot(stream, name),
                                errno = EPERM,
                            )
                        }
                    } catch (isDirectoryException: TempDirectoryNativeIOException) {
                        suppressedExceptions.addOrThrowNativeIOException(
                            errorText = "Unable to determine the file type",
                            filePath = pathDequeue.getPathFromRoot(stream, name),
                            errno = isDirectoryException.errno,
                            parent = isDirectoryException,
                        )
                        platformUnlinkDirectory(stream.dirfd, name) // Try to unlink directory, ignore errors
                    }

                    else -> suppressedExceptions.addOrThrowNativeIOException(
                        errorText = "Failed to delete file or directory",
                        filePath = pathDequeue.getPathFromRoot(stream, name),
                        errno = EPERM,
                    )
                }
            }
        }
    }

    private fun close() {
        pathDequeue.close()
    }
}
