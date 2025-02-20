/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.wasip1

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.TempfolderWasiIOException
import at.released.tempfolder.path.WasiPathString
import at.released.tempfolder.path.WasiPathString.Companion.toWasiPathString
import at.released.tempfolder.wasip1.type.Errno
import at.released.tempfolder.wasip1.type.LookupflagsFlag
import at.released.tempfolder.wasip1.type.OflagsFlag
import at.released.tempfolder.wasip1.type.RightsFlag
import at.released.tempfolder.wasip1.type.RightsFlag.DIRECTORY_INHERITING_RIGHTS
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.buildByteString
import kotlinx.io.bytestring.indices
import kotlin.wasm.unsafe.MemoryAllocator
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.withScopedMemoryAllocator

internal fun wasiCloseOrThrow(fd: TempDirectoryDescriptor) {
    wasiFdClose(fd.fd).throwIoExceptionOnError("fd_close() failed")
}

internal fun wasiOpenDirectoryOrThrow(
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
        oflags = OflagsFlag.DIRECTORY.toUInt(),
        fsRightsBase = RightsFlag.DIRECTORY_BASE_RIGHTS,
        fsRightsInheriting = DIRECTORY_INHERITING_RIGHTS,
        fdflags = 0U,
        expectedFd = fdPtr.address,
    ).throwIoExceptionOnError("path_open() failed")
    return TempDirectoryDescriptor(fdPtr.loadInt())
}

internal fun wasiCreateDirectoryOrThrow(
    fd: TempDirectoryDescriptor,
    path: WasiPathString,
): Unit = withScopedMemoryAllocator { allocator ->
    val (pathBytes, pathBytesSize) = allocator.allocateString(path)
    wasiPathCreateDirectory(fd.fd, pathBytes.address, pathBytesSize)
        .throwIoExceptionOnError("path_create_directory() failed")
}

internal fun wasiUnlinkDirectoryOrThrow(
    fd: TempDirectoryDescriptor,
    path: ByteString,
    throwIfNotExists: Boolean = true,
): Unit = withScopedMemoryAllocator { allocator ->
    val (pathBytes, pathBytesSize) = allocator.allocateString(path)
    val error = wasiPathRemoveDirectory(fd.fd, pathBytes.address, pathBytesSize)
    if (throwIfNotExists || error != Errno.NOENT.code) {
        error.throwIoExceptionOnError("path_create_directory() failed")
    }
}

internal fun wasiUnlinkFileOrThrow(
    fd: TempDirectoryDescriptor,
    path: ByteString,
    throwIfNotExists: Boolean = true,
): Unit = withScopedMemoryAllocator { allocator ->
    val (pathBytes, pathBytesSize) = allocator.allocateString(path)
    val error = wasiPathUnlinkFile(fd.fd, pathBytes.address, pathBytesSize)
    if (throwIfNotExists || error != Errno.NOENT.code) {
        error.throwIoExceptionOnError("path_unlink_file() failed")
    }
}

internal fun Pointer.loadPathString(length: Int): WasiPathString {
    if (length <= 0) {
        throw TempfolderIOException("Incorrect string length $length")
    }
    return loadByteString(length).toWasiPathString()
}

internal fun Pointer.loadByteString(length: Int): ByteString {
    return buildByteString(length) {
        repeat(length) { index -> append((this@loadByteString + index).loadByte()) }
    }
}

internal fun MemoryAllocator.allocateString(string: WasiPathString): Pair<Pointer, Int> = allocateString(string.bytes)

internal fun MemoryAllocator.allocateString(bytes: ByteString): Pair<Pointer, Int> {
    val ptr = allocate(bytes.size)
    for (index in bytes.indices) {
        (ptr + index).storeByte(bytes[index])
    }
    return ptr to bytes.size
}

internal fun Int.throwIoExceptionOnError(messagePrefix: String) {
    if (this != Errno.SUCCESS.code) {
        throw TempfolderWasiIOException.prefixed(this, messagePrefix)
    }
}
