/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.path.PosixPath
import at.released.tempfolder.posix200809.path.toPosixPath
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UnsafeNumber
import platform.posix.O_DIRECTORY
import platform.posix.O_NOFOLLOW
import platform.posix.O_NONBLOCK
import platform.posix.errno
import platform.posix.fchdir
import platform.posix.free
import platform.posix.getcwd
import platform.posix.open

@Throws(TempDirectoryIOException::class)
internal fun TempDirectoryDescriptor.getRealPath(): PosixPath {
    val targetFd = this.fd
    return openCurrentDirectory().use { currentDir ->
        CurrentDirectoryChanger(
            initialDirectoryFd = currentDir.fd,
            newDirectoryFd = targetFd,
        ).use {
            getCurrentWorkingDirectory()
        }
    }
}

private fun openCurrentDirectory(): CloseableFileDescriptor {
    val currentDirFd = open(".", O_DIRECTORY or O_NOFOLLOW or O_NONBLOCK)
    if (currentDirFd == -1) {
        throw TempDirectoryNativeIOException(errno, "Can not open current directory. ${errnoDescription()}")
    }
    return CloseableFileDescriptor(currentDirFd)
}

private class CurrentDirectoryChanger private constructor(
    private val initialDirectoryFd: Int,
) : AutoCloseable {
    override fun close() {
        changeCurrentDirectory(initialDirectoryFd, "Can not restore current directory.")
    }

    public companion object {
        @Throws(TempDirectoryIOException::class)
        operator fun invoke(initialDirectoryFd: Int, newDirectoryFd: Int): CurrentDirectoryChanger {
            changeCurrentDirectory(newDirectoryFd)
            return CurrentDirectoryChanger(initialDirectoryFd)
        }

        @Throws(TempDirectoryIOException::class)
        private fun changeCurrentDirectory(
            newDirectory: Int,
            onErrorText: String = "Can not change current directory.",
        ) {
            if (fchdir(newDirectory) == -1) {
                throw TempDirectoryNativeIOException(errno, "$onErrorText ${errnoDescription()}")
            }
        }
    }
}

@Throws(TempDirectoryIOException::class)
private fun getCurrentWorkingDirectory(): PosixPath {
    @OptIn(UnsafeNumber::class)
    val cwd: CPointer<ByteVar> = getcwd(null, 0U)
        ?: throw TempDirectoryNativeIOException(errno, "Can not get current working directory. ${errnoDescription()}")

    val cwdString = try {
        cwd.toPosixPath()
    } finally {
        free(cwd)
    }
    return cwdString
}
