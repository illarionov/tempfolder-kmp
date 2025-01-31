/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.DeleteRecursivelyException
import at.released.tempfolder.DeleteRecursivelyException.Companion.FAILED_TO_DELETE_MESSAGE
import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.internal.asPathString
import kotlinx.atomicfu.atomic
import kotlinx.io.bytestring.ByteString
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import at.released.tempfolder.InvalidPathException as TempfolderInvalidPathException

internal actual fun createPlatformTempFolder(namePrefix: String): Tempfolder<*> {
    return NioTempfolder.create(namePrefix)
}

public class NioTempfolder private constructor(
    absolutePath: Path,
) : Tempfolder<Path> {
    override val root: Path = absolutePath
    private val isClosed = atomic(false)
    private val _deleteOnClose = atomic(true)
    override var deleteOnClose: Boolean by _deleteOnClose

    private val absolutePathString: TempfolderPathString by lazy(PUBLICATION) {
        root.toString().asPathString()
    }

    override fun getAbsolutePath(): TempfolderPathString = absolutePathString

    @OptIn(ExperimentalPathApi::class)
    @Throws(TempfolderIOException::class)
    override fun delete() {
        throwIfClosed()
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

    @Throws(TempfolderIOException::class, CharacterCodingException::class)
    override fun resolve(name: ByteString): TempfolderPathString {
        val nameString = name.toByteArray().decodeToString(throwOnInvalidSequence = true)
        return try {
            root.resolve(nameString).toString().asPathString()
        } catch (ipe: InvalidPathException) {
            throw TempfolderInvalidPathException(ipe)
        }
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
        public fun create(
            namePrefix: String,
        ): NioTempfolder {
            val hasPosixFilePermissionSupport = FileSystems.getDefault().supportedFileAttributeViews().contains("posix")
            // TODO: move permissions to common
            val attrs: Array<FileAttribute<*>> = if (hasPosixFilePermissionSupport) {
                arrayOf(
                    PosixFilePermissions.asFileAttribute(
                        setOf(
                            PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                        ),
                    ),
                )
            } else {
                emptyArray()
            }

            @Suppress("SpreadOperator")
            // TODO: pass filesystem
            // TODO: parent dir
            val folder = Files.createTempDirectory(namePrefix, *attrs)
            return NioTempfolder(folder)
        }
    }
}
