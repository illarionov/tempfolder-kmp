/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.TempfolderException
import at.released.tempfolder.blocking.Tempfolder
import at.released.tempfolder.blocking.delete.deleteRecursively
import at.released.tempfolder.blocking.fd.WasiTempRootResolver.WasiP1TempRoot
import at.released.tempfolder.blocking.generateTempDirectoryName
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.WasiPathString
import at.released.tempfolder.wasip1.wasiUnlinkDirectoryOrThrow
import kotlinx.atomicfu.atomic
import kotlin.LazyThreadSafetyMode.PUBLICATION

public fun Tempfolder.Companion.createWasiP1TempDirectory(
    block: Wasip1TempDirectoryConfig.() -> Unit,
): Tempfolder<TempDirectoryDescriptor> {
    val config = Wasip1TempDirectoryConfig().apply(block)
    val root: WasiP1TempRoot = WasiTempRootResolver().resolve(config.base)
    try {
        val coordinates = WasiP1TempDirectoryCreator.createDirectory(
            root = root.fd,
            nameGenerator = { generateTempDirectoryName(config.prefix) },
        )
        return WasiP1TempDirectory(root, coordinates.pathnameFromTempRoot, coordinates.descriptor)
    } catch (err: TempfolderException) {
        try {
            root.close()
        } catch (closeErr: TempfolderException) {
            err.addSuppressed(closeErr)
        }
        throw err
    }
}

internal class WasiP1TempDirectory internal constructor(
    private val tempRoot: WasiP1TempRoot,
    private val directoryPathname: WasiPathString.Component,
    override val root: TempDirectoryDescriptor,
) : Tempfolder<TempDirectoryDescriptor> {
    private val isClosed = atomic(false)
    override var deleteOnClose: Boolean by atomic(true)
    private val fullPath: WasiPathString by lazy(PUBLICATION) {
        if (tempRoot.fullpath.asString() != ".") {
            tempRoot.fullpath.append(directoryPathname.asString())
        } else {
            directoryPathname
        }
    }

    override fun getAbsolutePath(): TempfolderPathString {
        throwIfClosed()
        return fullPath
    }

    override fun delete() {
        throwIfClosed()
        deleteUnprotected(root)
    }

    override fun append(name: String): TempfolderPathString {
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
        } catch (ex: TempfolderException) {
            exception = ex
            throw ex
        } finally {
            if (exception == null) {
                tempRoot.close()
            } else {
                try {
                    tempRoot.close()
                } catch (closeEx: TempfolderException) {
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
            throw TempfolderClosedException(TEMPFOLDER_CLOSED_MESSAGE)
        }
    }
}
