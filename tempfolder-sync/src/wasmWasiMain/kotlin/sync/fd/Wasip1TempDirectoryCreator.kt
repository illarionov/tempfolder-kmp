/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.fd

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.TempDirectoryWasiIOException
import at.released.tempfolder.path.WasiPath
import at.released.tempfolder.path.WasiPath.Companion.asWasiPathComponent
import at.released.tempfolder.path.WasiPath.Companion.toWasiPathString
import at.released.tempfolder.sync.MAX_CREATE_DIRECTORY_ATTEMPTS
import at.released.tempfolder.sync.generateTempDirectoryName
import at.released.tempfolder.wasip1.type.Errno
import at.released.tempfolder.wasip1.wasiCreateDirectoryOrThrow
import at.released.tempfolder.wasip1.wasiOpenDirectoryOrThrow
import at.released.tempfolder.wasip1.wasiUnlinkDirectoryOrThrow

internal object Wasip1TempDirectoryCreator {
    internal fun createDirectory(
        root: TempDirectoryDescriptor,
        nameGenerator: () -> String = { generateTempDirectoryName("tempfolder-") },
    ): TempDirectoryCoordinates {
        val tempDirectoryFd = (1..MAX_CREATE_DIRECTORY_ATTEMPTS).firstNotNullOfOrNull {
            val directoryName = nameGenerator().toWasiPathString().asWasiPathComponent()
            tryCreateTempDirectory(root, directoryName)
        }
        return tempDirectoryFd ?: throw TempDirectoryIOException("Can not create temp folder: max attempts reached")
    }

    private fun tryCreateTempDirectory(
        dirfd: TempDirectoryDescriptor,
        directoryName: WasiPath.Component,
    ): TempDirectoryCoordinates? {
        val directoryCreated = try {
            wasiCreateDirectoryOrThrow(dirfd, directoryName)
            true
        } catch (e: TempDirectoryWasiIOException) {
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
        } catch (err: TempDirectoryWasiIOException) {
            try {
                wasiUnlinkDirectoryOrThrow(dirfd, directoryName.bytes)
            } catch (unlinkErr: TempDirectoryException) {
                err.addSuppressed(unlinkErr)
            }
            throw err
        }
    }

    internal class TempDirectoryCoordinates(
        val pathnameFromTempRoot: WasiPath.Component,
        val descriptor: TempDirectoryDescriptor,
    )
}
