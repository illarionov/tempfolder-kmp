/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("Filename")

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderNativeIOException
import at.released.tempfolder.TempfolderPosixFileDescriptor
import at.released.tempfolder.asFileDescriptor
import at.released.tempfolder.blocking.fd.CreateDirectoryResult.DirectoryExists
import at.released.tempfolder.blocking.fd.CreateDirectoryResult.Error
import at.released.tempfolder.blocking.fd.CreateDirectoryResult.Success
import at.released.tempfolder.blocking.fd.PosixTempfolderBaseResolver.ResolvedBase
import at.released.tempfolder.blocking.fd.PosixTempfolderBaseResolver.ResolvedBase.FileDescriptor
import at.released.tempfolder.blocking.fd.PosixTempfolderBaseResolver.ResolvedBase.Path
import at.released.tempfolder.blocking.generateTempDirectoryName
import at.released.tempfolder.blocking.nativefunc.errnoDescription
import at.released.tempfolder.blocking.nativefunc.unlinkDirectory
import at.released.tempfolder.dsl.AdvisoryLockType
import at.released.tempfolder.dsl.AdvisoryLockType.EXCLUSIVE
import at.released.tempfolder.dsl.AdvisoryLockType.NONE
import at.released.tempfolder.dsl.AdvisoryLockType.SHARED
import at.released.tempfolder.dsl.TempfolderPosixBasePath
import at.released.tempfolder.path.PosixPathString
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.allocNullTerminatedPath
import at.released.tempfolder.path.toPosixPathString
import at.released.tempfolder.platform.linux.AT_FDCWD
import at.released.tempfolder.platform.linux.RESOLVE_BENEATH
import at.released.tempfolder.platform.linux.SYS_openat2
import at.released.tempfolder.platform.linux.mkdirat
import at.released.tempfolder.platform.linux.open_how
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.linux.flock
import platform.posix.EEXIST
import platform.posix.LOCK_EX
import platform.posix.LOCK_SH
import platform.posix.O_DIRECTORY
import platform.posix.O_NOFOLLOW
import platform.posix.O_NONBLOCK
import platform.posix.errno
import platform.posix.memset
import platform.posix.mode_t
import platform.posix.syscall

private const val MAX_ATTEMPTS = 100

@Throws(TempfolderIOException::class, TempfolderInvalidPathException::class)
internal fun createTempfolder(
    parent: TempfolderPosixBasePath,
    mode: mode_t = 0b000_111_000_000U,
    advisoryLock: AdvisoryLockType = SHARED,
    randomNameGenerator: () -> String = { generateTempDirectoryName("tempfolder-") },
): DescriptorWithPath {
    val base: ResolvedBase = PosixTempfolderBaseResolver.resolve(parent)
    val tempDirectoryFd = (1..MAX_ATTEMPTS).firstNotNullOfOrNull {
        val directoryName = randomNameGenerator().toPosixPathString()
        tryCreateTempfolder(base, directoryName, mode, advisoryLock)
    }
    return tempDirectoryFd ?: throw TempfolderIOException("Can not create temp folder: max attempts reached")
}

@Throws(TempfolderIOException::class)
private fun tryCreateTempfolder(
    base: ResolvedBase,
    directoryName: PosixPathString,
    mode: mode_t,
    advisoryLock: AdvisoryLockType,
): DescriptorWithPath? {
    val (dirFd: TempfolderPosixFileDescriptor, pathname: PosixPathString) = when (base) {
        is FileDescriptor -> base.fd to directoryName
        is Path -> AT_FDCWD.asFileDescriptor() to base.path.append(directoryName.asString())
    }

    when (val createDirectoryResult = tryCreateDirectory(dirFd, pathname, mode)) {
        DirectoryExists -> return null
        is Error -> throw createDirectoryResult.lastError
        Success -> Unit
    }

    try {
        val rootFd = openDirectory(dirFd, pathname, mode)
        setLock(rootFd, advisoryLock)
        return DescriptorWithPath(
            root = rootFd,
            absolutePath = if (base is Path) pathname else null,
        )
    } catch (ie: TempfolderNativeIOException) {
        val errno = unlinkDirectory(dirFd, pathname)
        if (errno != 0) {
            ie.addSuppressed(
                TempfolderNativeIOException(errno, "Can not remove temp directory. ${errnoDescription()}"),
            )
        }
        throw ie
    }
}

private fun tryCreateDirectory(
    base: TempfolderPosixFileDescriptor,
    directoryName: PosixPathString,
    mode: mode_t,
): CreateDirectoryResult {
    val mkdirResult = memScoped {
        val pathBytes = allocNullTerminatedPath(directoryName)
        mkdirat(base.fd, pathBytes, mode)
    }
    return CreateDirectoryResult.create(mkdirResult)
}

@Throws(TempfolderIOException::class)
private fun openDirectory(
    base: TempfolderPosixFileDescriptor,
    path: PosixPathString,
    mode: mode_t,
): TempfolderPosixFileDescriptor {
    val fd = memScoped {
        val pathBytes = allocNullTerminatedPath(path)
        val openHow: open_how = alloc<open_how> {
            memset(ptr, 0, sizeOf<open_how>().toULong())
            this.flags = (O_DIRECTORY or O_NOFOLLOW or O_NONBLOCK).toULong()
            this.mode = mode.toULong()
            this.resolve = RESOLVE_BENEATH.toULong()
        }
        syscall(
            __sysno = SYS_openat2.toLong(),
            base.fd,
            pathBytes,
            openHow.ptr,
            sizeOf<open_how>().toULong(),
        )
    }
    if (fd != -1L) {
        return TempfolderPosixFileDescriptor(fd.toInt())
    } else {
        throw TempfolderNativeIOException(errno, "Can not open created directory. ${errnoDescription()}")
    }
}

@Throws(TempfolderIOException::class)
private fun setLock(fd: TempfolderPosixFileDescriptor, type: AdvisoryLockType) {
    val lockOp = when (type) {
        NONE -> return
        EXCLUSIVE -> LOCK_EX
        SHARED -> LOCK_SH
    }
    val resultCode = flock(fd.fd, lockOp)
    if (resultCode == -1) {
        throw TempfolderNativeIOException(errno, "Can not set advisory lock on directory. ${errnoDescription()}")
    }
}

private sealed class CreateDirectoryResult {
    data object Success : CreateDirectoryResult()
    data object DirectoryExists : CreateDirectoryResult()
    class Error(val lastError: Exception) : CreateDirectoryResult()

    companion object {
        fun create(result: Int, error: Int = errno): CreateDirectoryResult = when {
            result != -1 -> Success
            error == EEXIST -> DirectoryExists
            else -> Error(
                TempfolderNativeIOException(error, "Failed to open temp directory. ${errnoDescription(error)}"),
            )
        }
    }
}

internal class DescriptorWithPath(
    val root: TempfolderPosixFileDescriptor,
    val absolutePath: PosixPathString?,
) {
    operator fun component1(): TempfolderPosixFileDescriptor = root
    operator fun component2(): PosixPathString? = absolutePath
}
