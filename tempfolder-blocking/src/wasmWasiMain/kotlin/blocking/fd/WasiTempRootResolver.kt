/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderWasiIOException
import at.released.tempfolder.blocking.fd.WasiP1TempBase.Auto
import at.released.tempfolder.blocking.fd.WasiP1TempBase.FileDescriptor
import at.released.tempfolder.blocking.fd.WasiP1TempBase.Path
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.WasiPathString
import at.released.tempfolder.path.WasiPathString.Companion.toWasiPathString
import at.released.tempfolder.wasip1.WasiPreopens
import at.released.tempfolder.wasip1.type.Errno
import at.released.tempfolder.wasip1.wasiCloseOrThrow
import at.released.tempfolder.wasip1.wasiLoadEnv
import at.released.tempfolder.wasip1.wasiOpenDirectoryOrThrow
import kotlin.LazyThreadSafetyMode.NONE

internal class WasiTempRootResolver {
    private val preopens: WasiPreopens by lazy(NONE) { WasiPreopens.load() }

    internal fun resolve(parent: WasiP1TempBase): WasiP1TempRoot {
        return when (parent) {
            Auto -> resolveAutoPath()
            is FileDescriptor -> WasiP1TempRoot(
                fd = TempDirectoryDescriptor(parent.fd),
                needClose = false,
                fullpath = ".".toWasiPathString(),
            )

            is Path -> tryResolvePath(parent.path.toWasiPathString())
                ?: throw TempfolderIOException("Path `${parent.path}` not resolvable")
        }
    }

    private fun resolveAutoPath(): WasiP1TempRoot {
        val tmpdir = try {
            wasiLoadEnv("TMPDIR")?.toWasiPathString()
        } catch (_: TempfolderInvalidPathException) {
            null
        }

        val tempRoot = listOfNotNull(
            tmpdir,
            "/tmp".toWasiPathString(),
            "/var/tmp".toWasiPathString(),
        ).firstNotNullOfOrNull(::tryResolvePath)

        if (tempRoot != null) {
            return tempRoot
        }

        if (preopens.size == 1) {
            val (preopenFd, preopenPath) = preopens.single()
            return WasiP1TempRoot(preopenFd, false, preopenPath)
        }
        throw TempfolderIOException("Can not resolve temp root. No suitable pre-opened directories")
    }

    private fun tryResolvePath(
        path: WasiPathString,
    ): WasiP1TempRoot? {
        val pathString = path.asString()
        return preopens.rootForPath(path).firstNotNullOfOrNull { preopen ->
            val (preopenfd, preopenPath) = preopen
            val relativePath = pathString.removePrefix(preopenPath.asString()).trimStart('/')
            if (relativePath.isEmpty()) {
                return@firstNotNullOfOrNull WasiP1TempRoot(preopenfd, false, preopenPath)
            }
            try {
                val fd = wasiOpenDirectoryOrThrow(preopenfd, relativePath.toWasiPathString())
                WasiP1TempRoot(
                    fd = fd,
                    needClose = true,
                    fullpath = path,
                )
            } catch (ioException: TempfolderWasiIOException) {
                when (ioException.wasiErrno) {
                    Errno.NOENT.code, Errno.NOTDIR.code -> null
                    else -> throw ioException
                }
            }
        }
    }

    internal class WasiP1TempRoot(
        val fd: TempDirectoryDescriptor,
        private var needClose: Boolean,
        val fullpath: WasiPathString,
    ) : AutoCloseable {
        override fun close() {
            if (!needClose) {
                return
            }

            needClose = false
            wasiCloseOrThrow(fd)
        }
    }
}
