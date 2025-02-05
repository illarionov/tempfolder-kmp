/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.nativefunc.delete

import at.released.tempfolder.DeleteRecursivelyException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderNativeIOException
import at.released.tempfolder.TempfolderPosixFileDescriptor
import at.released.tempfolder.asFileDescriptor
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirEntryType.DIRECTORY
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirEntryType.OTHER
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirEntryType.UNKNOWN
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirStreamItem.EndOfStream
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirStreamItem.Entry
import at.released.tempfolder.blocking.nativefunc.delete.DirStream.DirStreamItem.Error
import at.released.tempfolder.blocking.nativefunc.errnoDescription
import at.released.tempfolder.blocking.nativefunc.isDirectory
import at.released.tempfolder.blocking.nativefunc.unlinkDirectory
import at.released.tempfolder.blocking.nativefunc.unlinkFile
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.path.toPosixPathString
import at.released.tempfolder.util.runStackSuppressedExceptions
import kotlinx.cinterop.CPointer
import kotlinx.io.bytestring.ByteString
import platform.posix.DIR
import platform.posix.EISDIR
import platform.posix.ENOENT
import platform.posix.EPERM
import platform.posix.close
import platform.posix.dirfd
import platform.posix.dup
import platform.posix.errno
import platform.posix.fdopendir
import platform.posix.rewinddir

@Throws(DeleteRecursivelyException::class)
internal fun deleteRecursively(
    root: TempfolderPosixFileDescriptor,
    maxFileDescriptors: Int = 64,
) {
    BottomUpFileTreeRemover(root, maxFileDescriptors).delete()
}

@Suppress("TooManyFunctions")
private class BottomUpFileTreeRemover(
    private val root: TempfolderPosixFileDescriptor,
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
        val dir: CPointer<DIR>? = fdopendir(dup)
        if (dir == null) {
            val opendirException = TempfolderNativeIOException(
                errno,
                "Can not open directory. ${errnoDescription()}`",
            )
            if (close(dup) == -1) {
                opendirException.addSuppressed(
                    TempfolderNativeIOException(
                        errno,
                        "Can not close descriptor. ${errnoDescription()}`",
                    ),
                )
            }
            throw opendirException
        }
        rewinddir(dir)
        stack.addLast(DirStream(dir))
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
                    val unlinkError = unlinkDirectory(dirfd(this.stream.dir).asFileDescriptor(), PATH_CURRENT_DIRECTORY)
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
        val errno = unlinkFile(dirfd(stream.dir).asFileDescriptor(), name)
        if (errno != 0) {
            addSuppressedNativeIOException("Can not remove file `${name.asStringOrDescription()}`")
        }
    }

    private fun handleUnknown(name: PosixPathString) {
        val streamFd: TempfolderPosixFileDescriptor = dirfd(stream.dir).asFileDescriptor()
        val errno = unlinkFile(streamFd, name)
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
            isDirectory(dirfd(stream.dir).asFileDescriptor(), name)
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
        val dir = fdopendir(root.fd)
        if (dir != null) {
            rewinddir(dir)
            stack.addLast(DirStream(dir))
            usedFds += 1
        } else {
            addSuppressedNativeIOException("Can not open directory `${name.asStringOrDescription()}`.")
            unlinkDirectory(dirfd(stream.dir).asFileDescriptor(), name) // ignore errors
        }
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
