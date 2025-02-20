/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderWasiIOException
import at.released.tempfolder.blocking.MAX_CREATE_DIRECTORY_ATTEMPTS
import at.released.tempfolder.blocking.generateTempDirectoryName
import at.released.tempfolder.path.WasiPathString
import at.released.tempfolder.path.WasiPathString.Companion.asWasiPathComponent
import at.released.tempfolder.path.WasiPathString.Companion.toWasiPathString
import at.released.tempfolder.wasip1.type.Errno
import at.released.tempfolder.wasip1.wasiCreateDirectoryOrThrow
import at.released.tempfolder.wasip1.wasiOpenDirectoryOrThrow
import at.released.tempfolder.wasip1.wasiUnlinkDirectoryOrThrow

internal object WasiP1TempDirectoryCreator {
    internal fun createDirectory(
        root: TempDirectoryDescriptor,
        nameGenerator: () -> String = { generateTempDirectoryName("tempfolder-") },
    ): TempDirectoryCoordinates {
        val tempDirectoryFd = (1..MAX_CREATE_DIRECTORY_ATTEMPTS).firstNotNullOfOrNull {
            val directoryName = nameGenerator().toWasiPathString().asWasiPathComponent()
            tryCreateTempDirectory(root, directoryName)
        }
        return tempDirectoryFd ?: throw TempfolderIOException("Can not create temp folder: max attempts reached")
    }

    private fun tryCreateTempDirectory(
        dirfd: TempDirectoryDescriptor,
        directoryName: WasiPathString.Component,
    ): TempDirectoryCoordinates? {
        val directoryCreated = try {
            wasiCreateDirectoryOrThrow(dirfd, directoryName)
            true
        } catch (e: TempfolderWasiIOException) {
            if (e.wasiErrno == Errno.EXIST.code) {
                false
            } else {
                throw e
            }
        }
        if (!directoryCreated) {
            return null
        }

        try {
            val tempdirFd = wasiOpenDirectoryOrThrow(dirfd, directoryName)
            return TempDirectoryCoordinates(
                pathnameFromTempRoot = directoryName,
                descriptor = tempdirFd,
            )
        } catch (err: TempfolderWasiIOException) {
            try {
                wasiUnlinkDirectoryOrThrow(dirfd, directoryName.bytes)
            } catch (unlinkErr: TempfolderException) {
                err.addSuppressed(unlinkErr)
            }
            throw err
        }
    }

    internal class TempDirectoryCoordinates(
        val pathnameFromTempRoot: WasiPathString.Component,
        val descriptor: TempDirectoryDescriptor,
    )
}
