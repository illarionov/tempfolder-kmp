/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("KDOC_WITHOUT_THROWS_TAG")

package at.released.tempfolder.sync.fd

import at.released.tempfolder.TempDirectoryClosedException
import at.released.tempfolder.TempDirectoryClosedException.Companion.TEMP_DIRECTORY_CLOSED_MESSAGE
import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.WasiPath
import at.released.tempfolder.sync.TempDirectory
import at.released.tempfolder.sync.delete.deleteRecursively
import at.released.tempfolder.sync.fd.WasiTempRootResolver.WasiP1TempRoot
import at.released.tempfolder.sync.generateTempDirectoryName
import at.released.tempfolder.wasip1.wasiUnlinkDirectoryOrThrow
import kotlinx.atomicfu.atomic
import kotlin.LazyThreadSafetyMode.PUBLICATION

internal class Wasip1TempDirectory private constructor(
    private val tempRoot: WasiP1TempRoot,
    private val directoryPathname: WasiPath.Component,
    override val root: TempDirectoryDescriptor,
) : TempDirectory<TempDirectoryDescriptor> {
    private val isClosed = atomic(false)
    override var deleteOnClose: Boolean by atomic(true)
    private val fullPath: WasiPath by lazy(PUBLICATION) {
        if (tempRoot.fullpath.asString() != ".") {
            tempRoot.fullpath.append(directoryPathname.asString())
        } else {
            directoryPathname
        }
    }

    override fun getAbsolutePath(): TempDirectoryPath {
        throwIfClosed()
        return fullPath
    }

    override fun delete() {
        throwIfClosed()
        deleteUnprotected(root)
    }

    override fun append(name: String): TempDirectoryPath {
        return fullPath.append(name)
    }

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }

        var exception: Exception? = null
        try {
            if (deleteOnClose) {
                deleteUnprotected(root)
            }
        } catch (ex: TempDirectoryException) {
            exception = ex
            throw ex
        } finally {
            if (exception == null) {
                tempRoot.close()
            } else {
                try {
                    tempRoot.close()
                } catch (closeEx: TempDirectoryException) {
                    exception.addSuppressed(closeEx)
                }
            }
        }
    }

    private fun deleteUnprotected(root: TempDirectoryDescriptor) {
        // Delete content of directory
        deleteRecursively(root)
        // Try to delete directory itself
        wasiUnlinkDirectoryOrThrow(tempRoot.fd, directoryPathname.bytes, throwIfNotExists = false)
    }

    private fun throwIfClosed() {
        if (isClosed.value) {
            throw TempDirectoryClosedException(TEMP_DIRECTORY_CLOSED_MESSAGE)
        }
    }

    internal companion object {
        fun create(
            block: Wasip1TempDirectoryConfig.() -> Unit,
        ): TempDirectory<TempDirectoryDescriptor> {
            val config = Wasip1TempDirectoryConfig().apply(block)
            val root: WasiP1TempRoot = WasiTempRootResolver().resolve(config.base)
            try {
                val coordinates = Wasip1TempDirectoryCreator.createDirectory(
                    root = root.fd,
                    nameGenerator = { generateTempDirectoryName(config.prefix) },
                )
                return Wasip1TempDirectory(root, coordinates.pathnameFromTempRoot, coordinates.descriptor)
            } catch (err: TempDirectoryException) {
                try {
                    root.close()
                } catch (closeErr: TempDirectoryException) {
                    err.addSuppressed(closeErr)
                }
                throw err
            }
        }
    }
}
