/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.DeleteRecursivelyException
import at.released.tempfolder.DeleteRecursivelyException.Companion.FAILED_TO_DELETE_MESSAGE
import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.toPosixPathString
import kotlinx.atomicfu.atomic
import java.io.IOException
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

public class NioTempDirectory private constructor(
    absolutePath: Path,
) : Tempfolder<Path> {
    override val root: Path = absolutePath
    private val isClosed = atomic(false)
    override var deleteOnClose: Boolean by atomic(true)
    private val absolutePathString: TempfolderPathString by lazy(PUBLICATION) {
        root.toString().toPosixPathString()
    }

    override fun getAbsolutePath(): TempfolderPathString = absolutePathString

    @Throws(TempfolderIOException::class)
    override fun delete() {
        throwIfClosed()
        deleteRecursively(root)
    }

    @Throws(TempfolderIOException::class, CharacterCodingException::class)
    override fun resolve(name: String): TempfolderPathString {
        return try {
            root.resolve(name).toString().toPosixPathString()
        } catch (ipe: InvalidPathException) {
            throw TempfolderInvalidPathException(ipe)
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
            throw TempfolderClosedException(TEMPFOLDER_CLOSED_MESSAGE)
        }
    }

    public companion object {
        @Throws(TempfolderException::class)
        public operator fun invoke(
            block: NioTempDirectoryConfig.() -> Unit,
        ): NioTempDirectory {
            val config = NioTempDirectoryConfig().apply(block)
            val path = createNioTempDirectory(
                base = config.base,
                mode = config.permissions,
                nameGenerator = { generateTempDirectoryName(config.prefix) },
            )
            return NioTempDirectory(path)
        }

        @OptIn(ExperimentalPathApi::class)
        private fun deleteRecursively(root: Path) {
            try {
                root.deleteRecursively()
            } catch (ioe: IOException) {
                val deleteException = ioe.suppressed.let { suppressed ->
                    if (suppressed.isEmpty()) {
                        DeleteRecursivelyException(ioe)
                    } else {
                        DeleteRecursivelyException(FAILED_TO_DELETE_MESSAGE).apply {
                            suppressed.forEach(::addSuppressed)
                        }
                    }
                }
                throw deleteException
            }
        }
    }
}
