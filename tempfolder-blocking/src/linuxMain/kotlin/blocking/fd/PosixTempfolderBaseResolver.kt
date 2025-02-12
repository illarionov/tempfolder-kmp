/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.dsl.TempfolderSizeEstimate
import at.released.tempfolder.dsl.TempfolderSizeEstimate.LARGE
import at.released.tempfolder.dsl.TempfolderSizeEstimate.SMALL
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.toPosixPathString
import at.released.tempfolder.posix200809.TempfolderPosixFileDescriptor
import at.released.tempfolder.posix200809.asFileDescriptor
import at.released.tempfolder.posix200809.dsl.TempfolderPosixBasePath
import at.released.tempfolder.posix200809.dsl.TempfolderPosixBasePath.Auto
import at.released.tempfolder.posix200809.dsl.TempfolderPosixBasePath.FileDescriptor
import at.released.tempfolder.posix200809.dsl.TempfolderPosixBasePath.Path
import at.released.tempfolder.posix200809.path.toPosixPathString
import at.released.tempfolder.posix200809.platformRealpath
import platform.posix.getenv

internal object PosixTempfolderBaseResolver {
    @Throws(TempfolderIOException::class)
    internal fun resolve(parent: TempfolderPosixBasePath): ResolvedBase {
        return when (parent) {
            is Auto -> ResolvedBase.Path(getDefaultPath(parent.sizeEstimate))
            is Path -> ResolvedBase.Path(platformRealpath(parent.path.toPosixPathString()))
            is FileDescriptor -> ResolvedBase.FileDescriptor(parent.fd.asFileDescriptor())
        }
    }

    @Throws(TempfolderIOException::class)
    private fun getDefaultPath(sizeEstimate: TempfolderSizeEstimate): PosixPathString {
        val tmpdir = try {
            getenv("TMPDIR")?.toPosixPathString()
        } catch (_: TempfolderInvalidPathException) {
            null
        }

        return if (tmpdir != null) {
            platformRealpath(tmpdir)
        } else {
            getDefaultTempRoot(sizeEstimate)
        }
    }

    private fun getDefaultTempRoot(sizeEstimate: TempfolderSizeEstimate): PosixPathString {
        return when (sizeEstimate) {
            SMALL -> "/tmp".toPosixPathString()
            LARGE -> "/var/tmp".toPosixPathString()
        }
    }

    internal sealed interface ResolvedBase {
        value class FileDescriptor(val fd: TempfolderPosixFileDescriptor) : ResolvedBase
        value class Path(val path: PosixPathString) : ResolvedBase
    }
}
