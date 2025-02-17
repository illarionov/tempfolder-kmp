/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.wasip1

import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.asFileDescriptor
import at.released.tempfolder.path.WasiPathString
import at.released.tempfolder.wasip1.type.Errno
import kotlin.wasm.unsafe.withScopedMemoryAllocator

internal class WasiPreopens private constructor(
    private val map: Map<TempDirectoryDescriptor, WasiPathString>,
) {
    val size: Int get() = map.size
    private val entries: List<Pair<TempDirectoryDescriptor, WasiPathString>> =
        map.entries.map { it.toPair() }

    fun single(): Pair<TempDirectoryDescriptor, WasiPathString> = map.entries.single().toPair()

    fun rootForPath(path: WasiPathString): List<Pair<TempDirectoryDescriptor, WasiPathString>> {
        val pathString = path.asString()
        return entries.filter { (_, preopenPath) ->
            pathString.startsWith(preopenPath.asString())
        }.sortedWith(COMPARATOR_LONGEST_THEN_FD_COMPARATOR)
    }

    internal companion object {
        private const val WASI_FIRST_PREOPEN_FD: Int = 3
        private const val PRESTAT_PACKED_SIZE = 8
        internal val COMPARATOR_LONGEST_THEN_FD_COMPARATOR: Comparator<Pair<TempDirectoryDescriptor, WasiPathString>> =
            compareByDescending<Pair<TempDirectoryDescriptor, WasiPathString>> { it.second.bytes.size }
                .thenBy { it.first.fd }

        fun load(): WasiPreopens {
            val preopens: MutableMap<TempDirectoryDescriptor, WasiPathString> = mutableMapOf()
            var fd = WASI_FIRST_PREOPEN_FD
            withScopedMemoryAllocator { allocator ->
                val preopenResult = allocator.allocate(PRESTAT_PACKED_SIZE)
                while (true) {
                    val error = wasiFdPrestatGet(fd, preopenResult.address)
                    if (error != Errno.SUCCESS.code) {
                        break
                    }
                    val preopenTag = preopenResult.loadInt()
                    val prNameLen = (preopenResult + 4).loadInt()
                    if (preopenTag != 0) {
                        throw TempfolderIOException("Unexpected preopened directory tag: $preopenTag")
                    }
                    val fdDescriptor = fd.asFileDescriptor()
                    val path = loadPreopenPath(fdDescriptor, prNameLen)
                    preopens[fdDescriptor] = path
                    fd += 1
                }
            }
            return WasiPreopens(preopens)
        }

        private fun loadPreopenPath(
            fd: TempDirectoryDescriptor,
            prNameLen: Int,
        ): WasiPathString = withScopedMemoryAllocator { allocator ->
            val path = allocator.allocate(prNameLen)
            wasiFdPrestatDirName(fd.fd, path.address, prNameLen.toUInt())
                .throwIoExceptionOnError("fd_prestat_dir_name() failed")
            return path.loadPathString(prNameLen)
        }
    }
}
