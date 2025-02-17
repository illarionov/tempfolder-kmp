/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.wasip1

import at.released.tempfolder.wasip1.WasiHandle
import at.released.tempfolder.wasip1.WasmPtr
import at.released.tempfolder.wasip1.type.Lookupflags

/**
 * WASI Preview1 function `path_symlink`
 *
 * Definition:
 * ```
 * ;;; Create a symbolic link.
 *   ;;; Note: This is similar to `symlinkat` in POSIX.
 *   (@interface func (export "path_symlink")
 *     ;;; The contents of the symbolic link.
 *     (param $old_path string)
 *     (param $fd $fd)
 *     ;;; The destination path at which to create the symbolic link.
 *     (param $new_path string)
 *     (result $error (expected (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "path_symlink")
internal external fun wasiPathSymlink(
    oldPath: WasmPtr,
    oldPathSize: Int,
    fd: WasiHandle,
    newPath: WasmPtr,
    newPathSize: Int,
): Int

/**
 * WASI Preview1 function `path_filestat_get`
 *
 * Definition:
 * ```
 * ;;; Return the attributes of a file or directory.
 *   ;;; Note: This is similar to `stat` in POSIX.
 *   (@interface func (export "path_filestat_get")
 *     (param $fd $fd)
 *     ;;; Flags determining the method of how the path is resolved.
 *     (param $flags $lookupflags)
 *     ;;; The path of the file or directory to inspect.
 *     (param $path string)
 *     ;;; The buffer where the file's attributes are stored.
 *     (result $error (expected $filestat (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "path_filestat_get")
internal external fun wasiPathFilestatGet(
    fd: WasiHandle,
    flags: Lookupflags,
    path: WasmPtr,
    pathSize: Int,
    expectedFilestat: WasmPtr,
): Int

/**
 * WASI Preview1 function `fd_write`
 *
 * Definition:
 * ```
 * ;;; Write to a file descriptor.
 *   ;;; Note: This is similar to `writev` in POSIX.
 *   ;;;
 *   ;;; Like POSIX, any calls of `write` (and other functions to read or write)
 *   ;;; for a regular file by other threads in the WASI process should not be
 *   ;;; interleaved while `write` is executed.
 *   (@interface func (export "fd_write")
 *     (param $fd $fd)
 *     ;;; List of scatter/gather vectors from which to retrieve data.
 *     (param $iovs $ciovec_array)
 *     (result $error (expected $size (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "fd_write")
internal external fun wasiFdWrite(
    fd: WasiHandle,
    iovsList: WasmPtr,
    iovsLength: Int,
    expectedSize: WasmPtr,
): Int
