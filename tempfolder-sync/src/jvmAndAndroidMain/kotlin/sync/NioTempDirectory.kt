/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:JvmName("NioTempDirectoryBuilder")

package at.released.tempfolder.sync

import at.released.tempfolder.TempDirectoryClosedException
import at.released.tempfolder.TempDirectoryClosedException.Companion.TEMP_DIRECTORY_CLOSED_MESSAGE
import at.released.tempfolder.TempDirectoryDeleteException
import at.released.tempfolder.TempDirectoryDeleteException.Companion.FAILED_TO_DELETE_MESSAGE
import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.toPosixPath
import kotlinx.atomicfu.atomic
import java.io.IOException
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * Creates a temporary directory on the file system with JVM-specific configuration [block].
 *
 * This implementation is based on the use of [Java NIO][java.nio.file.FileSystems]
 *
 * [TempDirectory.root] property represents a real [Path] of the temp directory.
 *
 * @throws TempDirectoryIOException on errors during directory creation.
 * @see [createTempDirectory]
 */
@Throws(TempDirectoryException::class)
public fun TempDirectory.Companion.createJvmTempDirectory(
    block: TempDirectoryNioConfig.() -> Unit,
): TempDirectory<Path> {
    val config = TempDirectoryNioConfig().apply(block)
    val path = createNioTempDirectory(
        base = config.base,
        mode = config.permissions,
        nameGenerator = { generateTempDirectoryName(config.prefix) },
    )
    return NioTempDirectory(path)
}

private class NioTempDirectory(
    absolutePath: Path,
) : TempDirectory<Path> {
    override val root: Path = absolutePath
    private val isClosed = atomic(false)
    override var deleteOnClose: Boolean by atomic(true)
    private val absolutePathString: TempDirectoryPath by lazy(PUBLICATION) {
        root.toString().toPosixPath()
    }

    override fun getAbsolutePath(): TempDirectoryPath = absolutePathString

    @Throws(TempDirectoryIOException::class)
    override fun delete() {
        throwIfClosed()
        deleteRecursively(root)
    }

    @Throws(TempDirectoryIOException::class)
    override fun append(name: String): TempDirectoryPath {
        return try {
            root.resolve(name).toString().toPosixPath()
        } catch (ipe: InvalidPathException) {
            throw TempDirectoryInvalidPathException(ipe)
        }
    }

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        if (deleteOnClose) {
            deleteRecursively(root)
        }
    }

    private fun throwIfClosed() {
        if (isClosed.value) {
            throw TempDirectoryClosedException(TEMP_DIRECTORY_CLOSED_MESSAGE)
        }
    }

    companion object {
        @OptIn(ExperimentalPathApi::class)
        private fun deleteRecursively(root: Path) {
            try {
                root.deleteRecursively()
            } catch (ioe: IOException) {
                val deleteException = ioe.suppressed.let { suppressed ->
                    if (suppressed.isEmpty()) {
                        TempDirectoryDeleteException(ioe)
                    } else {
                        TempDirectoryDeleteException(FAILED_TO_DELETE_MESSAGE).apply {
                            suppressed.forEach(::addSuppressed)
                        }
                    }
                }
                throw deleteException
            }
        }
    }
}
