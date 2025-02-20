/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.fd

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.TempDirectoryWasiIOException
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.WasiPath
import at.released.tempfolder.path.WasiPath.Companion.toWasiPathString
import at.released.tempfolder.sync.fd.TempDirectoryWasip1TempBase.Auto
import at.released.tempfolder.sync.fd.TempDirectoryWasip1TempBase.FileDescriptor
import at.released.tempfolder.sync.fd.TempDirectoryWasip1TempBase.Path
import at.released.tempfolder.wasip1.WasiPreopens
import at.released.tempfolder.wasip1.type.Errno
import at.released.tempfolder.wasip1.wasiCloseOrThrow
import at.released.tempfolder.wasip1.wasiLoadEnv
import at.released.tempfolder.wasip1.wasiOpenDirectoryOrThrow
import kotlin.LazyThreadSafetyMode.NONE

internal class WasiTempRootResolver {
    private val preopens: WasiPreopens by lazy(NONE) { WasiPreopens.load() }

    internal fun resolve(parent: TempDirectoryWasip1TempBase): WasiP1TempRoot {
        return when (parent) {
            Auto -> resolveAutoPath()
            is FileDescriptor -> WasiP1TempRoot(
                fd = TempDirectoryDescriptor(parent.fd),
                needClose = false,
                fullpath = ".".toWasiPathString(),
            )

            is Path -> tryResolvePath(parent.path.toWasiPathString())
                ?: throw TempDirectoryIOException("Path `${parent.path}` not resolvable")
        }
    }

    private fun resolveAutoPath(): WasiP1TempRoot {
        val tmpdir = try {
            wasiLoadEnv("TMPDIR")?.toWasiPathString()
        } catch (_: TempDirectoryInvalidPathException) {
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
        throw TempDirectoryIOException("Can not resolve temp root. No suitable pre-opened directories")
    }

    private fun tryResolvePath(
        path: WasiPath,
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
            } catch (ioException: TempDirectoryWasiIOException) {
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
        val fullpath: WasiPath,
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
