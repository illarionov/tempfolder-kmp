/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.wasip1

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.path.WasiPathString
import at.released.tempfolder.testframework.wasip1.type.Ciovec
import at.released.tempfolder.testframework.wasip1.type.Filestat
import at.released.tempfolder.wasip1.allocateS32
import at.released.tempfolder.wasip1.allocateString
import at.released.tempfolder.wasip1.throwIoExceptionOnError
import at.released.tempfolder.wasip1.type.LookupflagsFlag
import at.released.tempfolder.wasip1.type.OflagsFlag
import at.released.tempfolder.wasip1.type.RightsFlag.FILE_BASE_RIGHTS
import at.released.tempfolder.wasip1.wasiPathOpen
import kotlinx.io.bytestring.ByteString
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.withScopedMemoryAllocator

internal fun wasiCreateFileOrThrow(
    fd: TempDirectoryDescriptor,
    path: WasiPathString,
    followSymlinks: Boolean = false,
): TempDirectoryDescriptor = withScopedMemoryAllocator { allocator ->
    val fdPtr = allocator.allocateS32()
    val (pathBytes, pathBytesSize) = allocator.allocateString(path)
    wasiPathOpen(
        fd = fd.fd,
        dirflags = if (followSymlinks) LookupflagsFlag.SYMLINK_FOLLOW else 0U,
        path = pathBytes.address,
        pathSize = pathBytesSize,
        oflags = (OflagsFlag.CREAT or OflagsFlag.EXCL).toUInt(),
        fsRightsBase = FILE_BASE_RIGHTS,
        fsRightsInheriting = FILE_BASE_RIGHTS,
        fdflags = 0U,
        expectedFd = fdPtr.address,
    ).throwIoExceptionOnError("path_open() failed")
    return TempDirectoryDescriptor(fdPtr.loadInt())
}

internal fun wasiPathSymlinkOrThrow(
    oldPath: ByteString,
    fd: TempDirectoryDescriptor,
    newPath: WasiPathString,
) = withScopedMemoryAllocator { allocator ->
    val (oldPathBytes, oldPathBytesSize) = allocator.allocateString(oldPath)
    val (newPathBytes, newPathBytesSize) = allocator.allocateString(newPath)
    wasiPathSymlink(oldPathBytes.address, oldPathBytesSize, fd.fd, newPathBytes.address, newPathBytesSize)
        .throwIoExceptionOnError("path_symlink() failed")
}

internal fun wasiPathFilestatGet(
    fd: TempDirectoryDescriptor,
    path: WasiPathString,
    followSymlinks: Boolean,
): Filestat = withScopedMemoryAllocator { allocator ->
    val (pathBytes, pathBytesSize) = allocator.allocateString(path)
    val filestat = allocator.allocate(FILESTAT_PACKED_SIZE)
    val lookupFlags = if (followSymlinks) LookupflagsFlag.SYMLINK_FOLLOW else 0U

    wasiPathFilestatGet(fd.fd, lookupFlags, pathBytes.address, pathBytesSize, filestat.address)
        .throwIoExceptionOnError("path_filestat_get() failed")

    Filestat.readFrom(filestat)
}

internal fun wasiFdWriteOrThrow(
    fd: TempDirectoryDescriptor,
    buffer: Pointer,
    size: Int,
): Int = withScopedMemoryAllocator { allocator ->
    val iovectsPtr = allocator.allocate(IOVEC_PACKED_SIZE)
    Ciovec(buffer.address.toInt(), size.toUInt()).writeTo(iovectsPtr)

    val expectedLength = allocator.allocateS32()

    wasiFdWrite(fd.fd, iovectsPtr.address, 1, expectedLength.address)
        .throwIoExceptionOnError("fd_write() failed")

    expectedLength.loadInt()
}
