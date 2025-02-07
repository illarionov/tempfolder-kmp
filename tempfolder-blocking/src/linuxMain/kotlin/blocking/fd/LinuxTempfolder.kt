/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.blocking.Tempfolder
import at.released.tempfolder.blocking.fd.delete.deleteRecursively
import at.released.tempfolder.blocking.generateTempDirectoryName
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
import at.released.tempfolder.posix200809.getRealPath
import at.released.tempfolder.posix200809.toPosixMode
import kotlinx.atomicfu.atomic

public class LinuxTempfolder private constructor(
    override val root: TempfolderPosixFileDescriptor,
    rootPath: PosixPathString?,
) : Tempfolder<TempfolderPosixFileDescriptor> {
    private val isClosed = atomic(false)
    override var deleteOnClose: Boolean by atomic(true)
    private val rootPath: Result<PosixPathString> by lazy {
        if (rootPath != null) {
            Result.success(rootPath)
        } else {
            runCatching(root::getRealPath)
        }
    }

    init {
        rootPath?.bytes?.let { pathBytes ->
            check(pathBytes[pathBytes.size - 1] != '.'.code.toByte()) {
                "path should be canonical"
            }
        }
    }

    override fun getAbsolutePath(): TempfolderPathString {
        throwIfClosed()
        return rootPath.getOrThrow()
    }

    override fun delete() {
        deleteRecursively(root)
    }

    @Throws(TempfolderIOException::class, TempfolderInvalidPathException::class)
    override fun resolve(name: String): TempfolderPathString {
        return rootPath.getOrThrow().append(name)
    }

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        if (deleteOnClose) {
            delete()
        }
    }

    private fun throwIfClosed() {
        if (isClosed.value) {
            throw TempfolderClosedException(TEMPFOLDER_CLOSED_MESSAGE)
        }
    }

    public companion object {
        @Throws(TempfolderIOException::class, TempfolderInvalidPathException::class)
        public operator fun invoke(
            block: LinuxTempfolderConfig.() -> Unit,
        ): LinuxTempfolder {
            val config = LinuxTempfolderConfig().apply(block)
            val (root, path) = createTempfolder(
                parent = config.base,
                mode = config.permissions.toPosixMode(),
                advisoryLock = config.advisoryLock,
                randomNameGenerator = { generateTempDirectoryName(config.prefix) },
            )
            return LinuxTempfolder(root, path)
        }
    }
}
