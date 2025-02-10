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
import at.released.tempfolder.posix200809.OpenDirectoryAt
import at.released.tempfolder.posix200809.PlatformDirent
import at.released.tempfolder.posix200809.TempfolderNativeIOException
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
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
import kotlin.LazyThreadSafetyMode.NONE

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
    private val direntApi: PlatformDirent<D>,
    maxFileDescriptors: Int = 64,
    maxSuppressedExceptions: Int = 8,
) {
    private val suppressedExceptions = SuppressedExceptionCollector(maxSuppressedExceptions)
    private val pathDequeue = PathDequeue(direntApi, maxFileDescriptors)

    @Throws(TempfolderIOException::class)
    @Suppress("ThrowsCount")
    fun delete() {
        try {
            enterDirectoryOrThrow(root, PATH_CURRENT_DIRECTORY, PATH_CURRENT_DIRECTORY)
        } catch (tne: TempfolderNativeIOException) {
            throw TempfolderNativeIOException(tne.errno, "Can not open directory to remove", tne)
        }

        try {
            deleteUnsafe()
            suppressedExceptions.removeFirstOrNull()?.let {
                throw DeleteRecursivelyException(
                    "${it.message}. Suppressed exceptions may contain other errors",
                    it,
                )
            }
        } catch (ie: TempfolderException) {
            suppressedExceptions.addSuppressedToThrowable(ie)
            throw ie
        } finally {
            try {
                close()
            } catch (@Suppress("SwallowedException") ex: TempfolderException) {
                // Ignore errors
            }
        }
    }

    @Throws(TempfolderIOException::class)
    private fun deleteUnsafe() {
        while (pathDequeue.isNotEmpty()) {
            val stream = pathDequeue.last()
            when (val dirEntry = stream.readNext()) {
                is Entry -> handleEntry(dirEntry.name, dirEntry.type)
                is Error -> throw TempfolderIOException(
                    "Can not read ${pathDequeue.getPathFromRoot(stream).asStringOrDescription()}",
                    dirEntry.error,
                )

                EndOfStream -> handleEndOfStream()
            }
        }
    }

    @Throws(TempfolderIOException::class)
    private fun handleEndOfStream() {
        val dirStream = pathDequeue.last()
        val pathFromRoot by lazy(NONE) {
            pathDequeue.getPathFromRoot(dirStream)
        }

        val (rootFd, path) = when (dirStream) {
            is OpenDirStream<*> -> dirStream.dirfd to PATH_CURRENT_DIRECTORY
            is PreloadedDirStream -> root to pathFromRoot
        }

        val unlinkError = platformUnlinkDirectory(rootFd, path)
        if (unlinkError != 0 && unlinkError != ENOENT) {
            suppressedExceptions.addNativeIOException(
                errorText = "Can not remove directory",
                filePath = pathFromRoot,
                errno = unlinkError,
            )
        }
        pathDequeue.removeLast()
        dirStream.close()
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
                try {
                    enterDirectoryOrThrow(stream.dirfd, basename, basename)
                } catch (tne: TempfolderNativeIOException) {
                    suppressedExceptions.addNativeIOException(
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
                } catch (error: TempfolderNativeIOException) {
                    suppressedExceptions.addNativeIOException(
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

    @Throws(TempfolderNativeIOException::class)
    private fun enterDirectoryOrThrow(
        dirFd: TempfolderPosixFileDescriptor,
        pathFromDirFd: PosixPathString,
        basename: PosixPathStringComponent,
    ) {
        pathDequeue.reserveFileDescriptor(::preloadDirectory)
        val fd = openDirectoryAt(dirFd, pathFromDirFd, true)
        if (fd.fd == -1) {
            throw TempfolderNativeIOException(errno, "Can not open directory")
        }
        val dir = direntApi.openDirectoryStreamOrCloseFd(fd)
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
                val errno = platformUnlinkFile(dirfd, name)
                if (errno != 0) {
                    suppressedExceptions.addNativeIOException(
                        errorText = "Can not remove file",
                        filePath = pathDequeue.getPathFromRoot(this, name),
                        errno = errno,
                    )
                }
            }

            UNKNOWN -> {
                // Try to delete as a file
                val errno = platformUnlinkFile(dirfd, name)
                when (errno) {
                    0 -> Unit
                    ENOENT -> Unit // Ignore
                    EISDIR -> onDirectory(name) // EISDIR is Linux-specific
                    EPERM -> try {
                        if (isDirectory(dirfd, name)) {
                            onDirectory(name)
                        } else {
                            suppressedExceptions.addNativeIOException(
                                errorText = "Can not remove file or directory",
                                filePath = pathDequeue.getPathFromRoot(this, name),
                                errno = EPERM,
                            )
                        }
                    } catch (isDirectoryException: TempfolderNativeIOException) {
                        suppressedExceptions.addNativeIOException(
                            errorText = "Can not determine file type",
                            filePath = pathDequeue.getPathFromRoot(this, name),
                            errno = isDirectoryException.errno,
                            parent = isDirectoryException,
                        )
                        platformUnlinkDirectory(dirfd, name) // Try to unlink directory, ignore errors
                    }

                    else -> suppressedExceptions.addNativeIOException(
                        errorText = "Can not remove file or directory",
                        filePath = pathDequeue.getPathFromRoot(this, name),
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
