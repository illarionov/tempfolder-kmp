/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderPosixFileDescriptor
import at.released.tempfolder.asFileDescriptor
import at.released.tempfolder.blocking.nativefunc.errnoDescription
import at.released.tempfolder.dsl.TempfolderPosixBasePath
import at.released.tempfolder.dsl.TempfolderPosixBasePath.Auto
import at.released.tempfolder.dsl.TempfolderPosixBasePath.FileDescriptor
import at.released.tempfolder.dsl.TempfolderPosixBasePath.Path
import at.released.tempfolder.dsl.TempfolderSizeEstimate
import at.released.tempfolder.dsl.TempfolderSizeEstimate.LARGE
import at.released.tempfolder.dsl.TempfolderSizeEstimate.SMALL
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.allocNullTerminatedPath
import at.released.tempfolder.path.toPosixPathString
import at.released.tempfolder.platform.linux.realpath
import kotlinx.cinterop.memScoped
import platform.posix.free
import platform.posix.getenv

internal object PosixTempfolderBaseResolver {
    private val TempfolderSizeEstimate.tempDirectory: PosixPathString
        get() = when (this) {
            SMALL -> "/tmp".toPosixPathString()
            LARGE -> "/var/tmp".toPosixPathString()
        }

    @Throws(TempfolderIOException::class)
    internal fun resolve(parent: TempfolderPosixBasePath): ResolvedBase {
        return when (parent) {
            is Auto -> ResolvedBase.Path(getDefaultPath(parent.sizeEstimate))
            is Path -> ResolvedBase.Path(resolveAbsolutePath(parent.path.toPosixPathString()))
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
            resolveAbsolutePath(tmpdir)
        } else {
            sizeEstimate.tempDirectory
        }
    }

    @Throws(TempfolderIOException::class)
    private fun resolveAbsolutePath(
        path: PosixPathString,
    ): PosixPathString = memScoped {
        val pathNative = allocNullTerminatedPath(path)
        val realPath = realpath(pathNative, null)
            ?: throw TempfolderIOException("Can not expand path. ${errnoDescription()}")
        val pathString = realPath.toPosixPathString()
        free(realPath)
        return pathString
    }

    internal sealed interface ResolvedBase {
        value class FileDescriptor(val fd: TempfolderPosixFileDescriptor) : ResolvedBase
        value class Path(val path: PosixPathString) : ResolvedBase
    }
}
