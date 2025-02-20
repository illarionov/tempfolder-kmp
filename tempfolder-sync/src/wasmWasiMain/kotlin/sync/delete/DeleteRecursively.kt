/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.delete

import at.released.tempfolder.DeleteRecursivelyException
import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderWasiIOException
import at.released.tempfolder.path.WasiPathString
import at.released.tempfolder.path.WasiPathString.Companion.WASI_PATH_CURRENT_DIRECTORY
import at.released.tempfolder.path.WasiPathString.Companion.asWasiPathComponent
import at.released.tempfolder.path.WasiPathString.Companion.isWasiSpecialDirectory
import at.released.tempfolder.path.WasiPathString.Companion.toWasiPathString
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.sync.delete.DirStream.DirStreamItem
import at.released.tempfolder.sync.delete.DirStream.DirStreamItem.Entry
import at.released.tempfolder.sync.delete.DirStream.DirStreamItem.Error
import at.released.tempfolder.wasip1.wasiOpenDirectoryOrThrow
import at.released.tempfolder.wasip1.wasiUnlinkDirectoryOrThrow
import at.released.tempfolder.wasip1.wasiUnlinkFileOrThrow
import kotlinx.io.bytestring.ByteString

/**
 * Recursively deletes the contents of the directory referenced by the descriptor [root].
 * Does not delete the directory itself. Does not close the file descriptor.
 */
internal fun deleteRecursively(
    root: TempDirectoryDescriptor,
    maxFileDescriptors: Int = 64,
) {
    BottomUpFileTreeRemover(
        root = root,
        maxFileDescriptors = maxFileDescriptors,
        platformUnlinkDirectoryIfExists = { dirfd, path ->
            wasiUnlinkDirectoryOrThrow(dirfd, path, throwIfNotExists = false)
        },
        platformUnlinkFileIfExists = { dirfd, path ->
            wasiUnlinkFileOrThrow(dirfd, path, throwIfNotExists = false)
        },
    ).delete()
}

@Suppress("TooManyFunctions")
private class BottomUpFileTreeRemover(
    private val root: TempDirectoryDescriptor,
    maxFileDescriptors: Int = 64,
    maxSuppressedExceptions: Int = 8,
    private val platformUnlinkDirectoryIfExists: (TempDirectoryDescriptor, ByteString) -> Unit,
    private val platformUnlinkFileIfExists: (TempDirectoryDescriptor, ByteString) -> Unit,
) {
    private val suppressedExceptions = SuppressedExceptionCollector(maxSuppressedExceptions)
    private val pathDequeue = PathDequeue(maxFileDescriptors)

    @Suppress("ThrowsCount")
    fun delete() {
        try {
            enterDirectoryOrThrow(root, WASI_PATH_CURRENT_DIRECTORY.bytes)
        } catch (tne: TempfolderWasiIOException) {
            throw TempfolderWasiIOException(tne.wasiErrno, "Can not open directory to remove", tne)
        }

        try {
            deleteUnsafe()
            suppressedExceptions.removeFirstOrNull()?.let {
                throw DeleteRecursivelyException("${it.message}. Suppressed exceptions may contain other errors", it)
            }
        } catch (ie: TempfolderException) {
            suppressedExceptions.addSuppressedToThrowable(ie)
            throw ie
        } finally {
            closeSilent()
        }
    }

    private fun closeSilent() {
        try {
            close()
        } catch (@Suppress("SwallowedException") ex: TempfolderException) {
            // Ignore errors
        }
    }

    private fun deleteUnsafe() {
        while (pathDequeue.isNotEmpty()) {
            val stream = pathDequeue.last()
            when (val dirEntry = stream.readNext()) {
                is Entry -> handleEntry(dirEntry.name, dirEntry.isDirectory)
                is Error -> throw TempfolderIOException(
                    "Failed to read directory entry at ${pathDequeue.getPathFromRoot(stream).asStringOrDescription()}",
                    dirEntry.error,
                )

                DirStreamItem.EndOfStream -> handleEndOfStream()
            }
        }
    }

    private fun handleEndOfStream() {
        val closingStream = pathDequeue.removeLast()
        closingStream.close()
        if (pathDequeue.isNotEmpty()) {
            val parentStream = pathDequeue.last()

            val (rootFd, path) = when (parentStream) {
                is OpenDirStream -> parentStream.dirfd to closingStream.basename
                is PreloadedDirStream -> root to pathDequeue.getPathFromRoot(parentStream, closingStream.basename.bytes)
            }

            try {
                platformUnlinkDirectoryIfExists(rootFd, path.bytes)
            } catch (err: TempfolderWasiIOException) {
                suppressedExceptions.addOrThrowNativeIOException(
                    errorText = "Can not remove directory",
                    filePath = pathDequeue.getPathFromRoot(parentStream, closingStream.basename.bytes),
                    errno = err.wasiErrno,
                    parent = err,
                )
                parentStream.addIgnore(closingStream.basename.bytes)
            }
        }
    }

    private fun handleEntry(
        basename: ByteString,
        isDirectory: Boolean,
    ) {
        if (basename.isWasiSpecialDirectory()) {
            return
        }

        val stream = pathDequeue.last()

        when (stream) {
            is OpenDirStream -> if (!isDirectory) {
                unlinkFile(stream, basename)
            } else {
                try {
                    enterDirectoryOrThrow(stream.dirfd, basename)
                } catch (tne: TempfolderWasiIOException) {
                    suppressedExceptions.addOrThrowNativeIOException(
                        errorText = "Can not enter directory",
                        filePath = pathDequeue.getPathFromRoot(stream, basename),
                        errno = tne.wasiErrno,
                        parent = tne,
                    )
                    try {
                        platformUnlinkDirectoryIfExists(stream.dirfd, basename)
                    } catch (unlinkError: TempfolderWasiIOException) {
                        tne.addSuppressed(unlinkError)
                        stream.addIgnore(basename)
                    }
                }
            }

            is PreloadedDirStream -> {
                check(isDirectory) { "Unexpected entry type" }
                val path = pathDequeue.getPathFromRoot(stream, basename)
                try {
                    enterDirectoryOrThrow(root, path, basename)
                } catch (error: TempfolderWasiIOException) {
                    suppressedExceptions.addOrThrowNativeIOException(
                        errorText = "Can not enter directory",
                        filePath = path,
                        errno = error.wasiErrno,
                        parent = error,
                    )
                    try {
                        platformUnlinkDirectoryIfExists(root, path.bytes)
                    } catch (unlinkError: TempfolderWasiIOException) {
                        error.addSuppressed(unlinkError)
                    }
                }
            }
        }
    }

    private fun enterDirectoryOrThrow(
        dirFd: TempDirectoryDescriptor,
        basename: ByteString,
    ) {
        val component: WasiPathString.Component = basename.toWasiPathString().asWasiPathComponent()
        enterDirectoryOrThrow(dirFd, component, component)
    }

    private fun enterDirectoryOrThrow(
        dirFd: TempDirectoryDescriptor,
        pathFromDirFd: WasiPathString,
        basename: ByteString,
    ) {
        val basenameComponent: WasiPathString.Component = basename.toWasiPathString().asWasiPathComponent()
        enterDirectoryOrThrow(dirFd, pathFromDirFd, basenameComponent)
    }

    private fun enterDirectoryOrThrow(
        dirFd: TempDirectoryDescriptor,
        pathFromDirFd: WasiPathString,
        basename: WasiPathString.Component,
    ) {
        pathDequeue.reserveFileDescriptor(::preloadDirectory)
        val fd = wasiOpenDirectoryOrThrow(dirFd, pathFromDirFd, false)
        pathDequeue.addLast(fd, basename)
    }

    private fun preloadDirectory(
        stream: OpenDirStream,
    ): List<Entry> {
        val directories: MutableList<Entry> = mutableListOf()
        while (true) {
            when (val entry = stream.readNext()) {
                is Entry -> if (entry.isDirectory) {
                    directories.add(entry)
                    stream.addIgnore(entry.name)
                } else {
                    unlinkFile(stream, entry.name)
                }

                is Error -> throw TempfolderIOException(entry.error)
                DirStreamItem.EndOfStream -> break
            }
        }
        return directories
    }

    private fun unlinkFile(stream: OpenDirStream, name: ByteString) {
        try {
            platformUnlinkFileIfExists(stream.dirfd, name)
        } catch (err: TempfolderWasiIOException) {
            suppressedExceptions.addOrThrowNativeIOException(
                errorText = "Can not unlink file",
                filePath = pathDequeue.getPathFromRoot(stream, name),
                errno = err.wasiErrno,
                parent = err,
            )
            stream.addIgnore(name)
        }
    }

    private fun close() {
        pathDequeue.close()
    }
}
