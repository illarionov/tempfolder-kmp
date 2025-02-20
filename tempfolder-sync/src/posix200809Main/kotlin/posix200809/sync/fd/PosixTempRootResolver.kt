/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.sync.fd

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.asFileDescriptor
import at.released.tempfolder.dsl.TempDirectorySizeEstimate
import at.released.tempfolder.dsl.TempDirectorySizeEstimate.LARGE
import at.released.tempfolder.dsl.TempDirectorySizeEstimate.SMALL
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.toPosixPath
import at.released.tempfolder.posix200809.dsl.TempDirectoryPosixBase
import at.released.tempfolder.posix200809.dsl.TempDirectoryPosixBase.Auto
import at.released.tempfolder.posix200809.dsl.TempDirectoryPosixBase.FileDescriptor
import at.released.tempfolder.posix200809.dsl.TempDirectoryPosixBase.Path
import at.released.tempfolder.posix200809.path.toPosixPath
import at.released.tempfolder.posix200809.platformRealpath
import platform.posix.getenv

internal object PosixTempRootResolver {
    @Throws(TempDirectoryIOException::class)
    internal fun resolve(parent: TempDirectoryPosixBase): ResolvedTempRoot {
        return when (parent) {
            is Auto -> ResolvedTempRoot.Path(getDefaultPath(parent.sizeEstimate))
            is Path -> ResolvedTempRoot.Path(platformRealpath(parent.path.toPosixPath()))
            is FileDescriptor -> ResolvedTempRoot.FileDescriptor(parent.fd.asFileDescriptor())
        }
    }

    @Throws(TempDirectoryIOException::class)
    private fun getDefaultPath(sizeEstimate: TempDirectorySizeEstimate): PosixPath {
        val tmpdir = try {
            getenv("TMPDIR")?.toPosixPath()
        } catch (_: TempDirectoryInvalidPathException) {
            null
        }

        return if (tmpdir != null) {
            platformRealpath(tmpdir)
        } else {
            getDefaultTempRoot(sizeEstimate)
        }
    }

    private fun getDefaultTempRoot(sizeEstimate: TempDirectorySizeEstimate): PosixPath {
        return when (sizeEstimate) {
            SMALL -> "/tmp".toPosixPath()
            LARGE -> "/var/tmp".toPosixPath()
        }
    }

    internal sealed interface ResolvedTempRoot {
        value class FileDescriptor(val fd: TempDirectoryDescriptor) : ResolvedTempRoot
        value class Path(val path: PosixPath) : ResolvedTempRoot
    }
}
