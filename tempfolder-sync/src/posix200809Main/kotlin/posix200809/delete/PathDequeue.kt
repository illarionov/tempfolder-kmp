/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.delete

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.PATH_CURRENT_DIRECTORY
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.path.PosixPath.Companion.toPosixPath
import at.released.tempfolder.path.UNIX_PATH_SEPARATOR
import at.released.tempfolder.path.isCurrentDirectory
import at.released.tempfolder.posix200809.PlatformDirent
import at.released.tempfolder.posix200809.TempDirectoryNativeIOException
import at.released.tempfolder.posix200809.delete.DirStream.DirStreamItem
import kotlinx.io.bytestring.append
import kotlinx.io.bytestring.buildByteString
import kotlinx.io.bytestring.isNotEmpty

internal class PathDequeue<D>(
    private val dirent: PlatformDirent<D>,
    private val maxFileDescriptors: Int = 64,
) : AutoCloseable {
    private val path: ArrayDeque<StreamHolder> = ArrayDeque()
    private val openDirs: ArrayDeque<StreamHolder> = ArrayDeque(maxFileDescriptors)

    init {
        check(maxFileDescriptors >= 1)
    }

    fun isNotEmpty(): Boolean = path.isNotEmpty()

    fun reserveFileDescriptor(loadStreamFunc: (OpenDirStream<D>) -> List<DirStreamItem.Entry>) {
        if (openDirs.size < maxFileDescriptors) {
            return
        }
        val holder = openDirs.removeFirst()

        @Suppress("UNCHECKED_CAST")
        val stream = holder.stream as OpenDirStream<D>
        val entries = loadStreamFunc(stream)
        holder.stream = PreloadedDirStream(stream.basename, entries)
    }

    fun addLast(
        dir: D,
        dirfd: TempDirectoryDescriptor,
        basename: PosixPath.Component,
    ) {
        check(openDirs.size < maxFileDescriptors) { "No free file descriptor" }

        val dirStream = OpenDirStream(dirent, dir, dirfd, basename)
        val holder = StreamHolder(dirStream)
        path += holder
        openDirs += holder
    }

    fun last(): DirStream = path.last().stream

    fun removeLast(): DirStream {
        val holder = path.removeLast()
        if (holder.stream is OpenDirStream<*>) {
            openDirs.removeLast()
        }
        return holder.stream
    }

    fun getPathFromRoot(
        dirStream: DirStream,
        basename: PosixPath.Component = PATH_CURRENT_DIRECTORY,
    ): PosixPath {
        val dirStreamIndex = path.indexOfFirst { it.stream === dirStream }
        check(dirStreamIndex >= 0) { "No ${dirStream.basename} directory stream in deque" }
        val subpath = path.subList(1, dirStreamIndex + 1)

        val newSize: Int = subpath.sumOf { it.name.bytes.size + 1 } + basename.bytes.size
        val newBytes = buildByteString(newSize) {
            subpath.forEach {
                append(it.name.bytes)
                append(UNIX_PATH_SEPARATOR)
            }
            if (!basename.isCurrentDirectory()) {
                append(basename.bytes)
            }
        }
        return if (newBytes.isNotEmpty()) {
            newBytes.toPosixPath()
        } else {
            PATH_CURRENT_DIRECTORY
        }
    }

    // @Throws(TempfolderIOException::class)
    override fun close() {
        val exceptions = path.mapNotNull { dirStream ->
            try {
                dirStream.close()
                null
            } catch (closeDirException: TempDirectoryNativeIOException) {
                closeDirException
            }
        }
        if (exceptions.isNotEmpty()) {
            val ex = TempDirectoryIOException("Can not close directories")
            exceptions.take(3).forEach { ex.addSuppressed(it) }
            throw ex
        }
    }

    class StreamHolder(
        var stream: DirStream,
    ) : AutoCloseable {
        val name: PosixPath get() = stream.basename
        override fun close() = stream.close()
    }
}
