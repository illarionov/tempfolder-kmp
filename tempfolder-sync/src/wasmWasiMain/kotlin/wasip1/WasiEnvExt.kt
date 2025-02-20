/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(UnsafeWasmMemoryApi::class)

package at.released.tempfolder.wasip1

import at.released.tempfolder.TempDirectoryException
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

internal fun wasiLoadEnv(name: String): String? = withScopedMemoryAllocator { allocator ->
    val expectedNumArgs = allocator.allocateS32()
    val expectedSize = allocator.allocateS32()

    wasiEnvironSizesGet(expectedNumArgs.address, expectedSize.address)
        .throwIoExceptionOnError("wasiEnvironSizesGet() failed")

    val numArgs = expectedNumArgs.loadInt()
    when {
        numArgs == 0 -> return null
        numArgs > 1024 -> throw TempDirectoryException("the number of environment variables is too large")
    }

    val bufSize = expectedSize.loadInt().toUInt()
    when {
        bufSize < 3U -> return null
        bufSize > 2U * 1024U * 1024U ->
            throw TempDirectoryException("buffer for environment variables is suspiciously large")
    }

    val envPointers = allocator.allocate(numArgs * WASM_POINTER_SIZE)
    val buf = allocator.allocate(bufSize.toInt())

    wasiEnvironGet(envPointers.address, buf.address)
        .throwIoExceptionOnError("wasiEnvironSizesGet() failed")

    return (0 until numArgs).firstNotNullOfOrNull {
        val ptr = (envPointers + it * WASM_POINTER_SIZE).loadPointer()
        readEnvVariable(buf, bufSize, ptr, name)
    }
}

private fun readEnvVariable(
    buffer: Pointer,
    bufferSize: UInt,
    envPointer: Pointer,
    name: String,
): String? {
    if (envPointer.address !in buffer.address until buffer.address + bufferSize) {
        throw TempDirectoryException("pointer outside of buffer")
    }

    val stringSize = getNullTerminatedStringSize(
        envPointer,
        (buffer.address + bufferSize - envPointer.address).toInt(),
    )

    val envString = (ByteArray(stringSize) { (envPointer + it).loadByte() }).decodeToString()
    val key = envString.substringBefore("=")
    if (key != name) {
        return null
    }
    return envString.substringAfter("=", missingDelimiterValue = "")
}

private fun getNullTerminatedStringSize(pointer: Pointer, maxSize: Int): Int {
    var index = 0
    while (index < maxSize) {
        if ((pointer + index).loadByte().toInt() != 0) {
            index += 1
        } else {
            return index
        }
    }
    throw TempDirectoryException("String is not null-terminated")
}
