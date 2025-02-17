/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.delete

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.blocking.delete.DirStream.DirStreamItem
import at.released.tempfolder.path.TempfolderPathString.MultibytePathString
import at.released.tempfolder.path.UNIX_PATH_SEPARATOR
import at.released.tempfolder.path.WasiPathString
import at.released.tempfolder.path.WasiPathString.Companion.WASI_PATH_CURRENT_DIRECTORY
import at.released.tempfolder.path.WasiPathString.Companion.toWasiPathString
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.append
import kotlinx.io.bytestring.buildByteString
import kotlinx.io.bytestring.isNotEmpty

internal class PathDequeue(
    private val maxFileDescriptors: Int = 64,
) : AutoCloseable {
    private val path: ArrayDeque<StreamHolder> = ArrayDeque()
    private val openDirs: ArrayDeque<StreamHolder> = ArrayDeque(maxFileDescriptors)

    init {
        check(maxFileDescriptors >= 1)
    }

    fun isNotEmpty(): Boolean = path.isNotEmpty()

    fun reserveFileDescriptor(loadStreamFunc: (OpenDirStream) -> List<DirStreamItem.Entry>) {
        if (openDirs.size < maxFileDescriptors) {
            return
        }
        val holder = openDirs.removeFirst()

        val stream = holder.stream as OpenDirStream
        val entries = loadStreamFunc(stream)
        holder.stream = PreloadedDirStream(stream.basename, entries)
    }

    fun addLast(
        dirfd: TempDirectoryDescriptor,
        basename: WasiPathString.Component,
    ) {
        check(openDirs.size < maxFileDescriptors) { "No free file descriptor" }

        val dirStream = OpenDirStream(dirfd, basename)
        val holder = StreamHolder(dirStream)
        path += holder
        openDirs += holder
    }

    fun last(): DirStream = path.last().stream

    fun removeLast(): DirStream {
        val holder = path.removeLast()
        if (holder.stream is OpenDirStream) {
            openDirs.removeLast()
        }
        return holder.stream
    }

    fun getPathFromRoot(
        dirStream: DirStream,
        basename: ByteString? = null,
    ): WasiPathString {
        val dirStreamIndex = path.indexOfFirst { it.stream === dirStream }
        check(dirStreamIndex >= 0) { "No ${dirStream.basename} directory stream in deque" }
        val subpath = path.subList(1, dirStreamIndex + 1)

        val newSize: Int = subpath.sumOf { it.name.bytes.size + 1 } + (basename?.size ?: 0)
        val newBytes = buildByteString(newSize) {
            subpath.forEach {
                append(it.name.bytes)
                append(UNIX_PATH_SEPARATOR)
            }
            if (basename != null) {
                append(basename)
            }
        }
        return if (newBytes.isNotEmpty()) {
            newBytes.toWasiPathString()
        } else {
            WASI_PATH_CURRENT_DIRECTORY
        }
    }

    override fun close() {
        val exceptions = path.mapNotNull { dirStream ->
            try {
                dirStream.close()
                null
            } catch (closeDirException: TempfolderException) {
                closeDirException
            }
        }
        if (exceptions.isNotEmpty()) {
            val ex = TempfolderIOException("Can not close directories")
            exceptions.take(3).forEach { ex.addSuppressed(it) }
            throw ex
        }
    }

    class StreamHolder(
        var stream: DirStream,
    ) : AutoCloseable {
        val name: MultibytePathString get() = stream.basename
        override fun close() = stream.close()
    }
}
