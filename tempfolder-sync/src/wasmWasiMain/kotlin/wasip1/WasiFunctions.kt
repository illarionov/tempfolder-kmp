/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongParameterList", "MaxLineLength")

package at.released.tempfolder.wasip1

import at.released.tempfolder.wasip1.type.DircookieWasiType
import at.released.tempfolder.wasip1.type.FdflagsWasmType
import at.released.tempfolder.wasip1.type.LookupflagsWasmType
import at.released.tempfolder.wasip1.type.OflagsWasmType
import at.released.tempfolder.wasip1.type.RightsWasmType
import at.released.tempfolder.wasip1.type.SizeWasiType

/**
 * WASI Preview1 function imports
 *
 * https://github.com/WebAssembly/WASI/blob/main/legacy/preview1/witx/wasi_snapshot_preview1.witx
 */

internal typealias WasmPtr = UInt
internal typealias WasiHandle = Int
internal typealias WasiU16 = UInt
internal typealias WasiU32 = UInt
internal typealias WasiU64 = ULong
internal typealias WasiI32 = Int
internal typealias WasiI64 = Long

/**
 * WASI Preview1 function `environ_get`
 *
 * Definition:
 * ```
 * ;;; Read environment variable data.
 *   ;;; The sizes of the buffers should match that returned by `environ_sizes_get`.
 *   ;;; Key/value pairs are expected to be joined with `=`s, and terminated with `\0`s.
 *   (@interface func (export "environ_get")
 *     (param $environ (@witx pointer (@witx pointer u8)))
 *     (param $environ_buf (@witx pointer u8))
 *     (result $error (expected (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "environ_get")
internal external fun wasiEnvironGet(environ: WasmPtr, environBuf: WasmPtr): Int

/**
 * WASI Preview1 function `environ_sizes_get`
 *
 * Definition:
 * ```
 * ;;; Return environment variable data sizes.
 *   (@interface func (export "environ_sizes_get")
 *     ;;; Returns the number of environment variable arguments and the size of the
 *     ;;; environment variable data.
 *     (result $error (expected (tuple $size $size) (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "environ_sizes_get")
internal external fun wasiEnvironSizesGet(expectedNumArgs: WasmPtr, expectedSize: WasmPtr): Int

/**
 * WASI Preview1 function `fd_close`
 *
 * Definition:
 * ```
 * ;;; Close a file descriptor.
 *   ;;; Note: This is similar to `close` in POSIX.
 *   (@interface func (export "fd_close")
 *     (param $fd $fd)
 *     (result $error (expected (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "fd_close")
internal external fun wasiFdClose(fd: WasiHandle): Int

/**
 * WASI Preview1 function `fd_prestat_get`
 *
 * Definition:
 * ```
 * ;;; Return a description of the given preopened file descriptor.
 *   (@interface func (export "fd_prestat_get")
 *     (param $fd $fd)
 *     ;;; The buffer where the description is stored.
 *     (result $error (expected $prestat (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "fd_prestat_get")
internal external fun wasiFdPrestatGet(fd: WasiHandle, expectedPrestat: WasmPtr): Int

/**
 * WASI Preview1 function `fd_prestat_dir_name`
 *
 * Definition:
 * ```
 * ;;; Return a description of the given preopened file descriptor.
 *   (@interface func (export "fd_prestat_dir_name")
 *     (param $fd $fd)
 *     ;;; A buffer into which to write the preopened directory name.
 *     (param $path (@witx pointer u8))
 *     (param $path_len $size)
 *     (result $error (expected (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "fd_prestat_dir_name")
internal external fun wasiFdPrestatDirName(fd: WasiHandle, path: WasmPtr, pathLen: SizeWasiType): Int

/**
 * WASI Preview1 function `fd_readdir`
 *
 * Definition:
 * ```
 * ;;; Read directory entries from a directory.
 *   ;;; When successful, the contents of the output buffer consist of a sequence of
 *   ;;; directory entries. Each directory entry consists of a `dirent` object,
 *   ;;; followed by `dirent::d_namlen` bytes holding the name of the directory
 *   ;;; entry.
 *   ;;
 *   ;;; This function fills the output buffer as much as possible, potentially
 *   ;;; truncating the last directory entry. This allows the caller to grow its
 *   ;;; read buffer size in case it's too small to fit a single large directory
 *   ;;; entry, or skip the oversized directory entry.
 *   ;;;
 *   ;;; Entries for the special `.` and `..` directory entries are included in the
 *   ;;; sequence.
 *   (@interface func (export "fd_readdir")
 *     (param $fd $fd)
 *     ;;; The buffer where directory entries are stored
 *     (param $buf (@witx pointer u8))
 *     (param $buf_len $size)
 *     ;;; The location within the directory to start reading
 *     (param $cookie $dircookie)
 *     ;;; The number of bytes stored in the read buffer. If less than the size of the read buffer, the end of the directory has been reached.
 *     (result $error (expected $size (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "fd_readdir")
internal external fun wasiFdReaddir(
    fd: WasiHandle,
    buf: WasmPtr,
    bufLen: SizeWasiType,
    cookie: DircookieWasiType,
    expectedSize: WasmPtr,
): Int

/**
 * WASI Preview1 function `path_create_directory`
 *
 * Definition:
 * ```
 * ;;; Create a directory.
 *   ;;; Note: This is similar to `mkdirat` in POSIX.
 *   (@interface func (export "path_create_directory")
 *     (param $fd $fd)
 *     ;;; The path at which to create the directory.
 *     (param $path string)
 *     (result $error (expected (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "path_create_directory")
internal external fun wasiPathCreateDirectory(fd: WasiHandle, path: WasmPtr, pathSize: Int): Int

/**
 * WASI Preview1 function `path_open`
 *
 * Definition:
 * ```
 * ;;; Open a file or directory.
 *   ;;
 *   ;;; The returned file descriptor is not guaranteed to be the lowest-numbered
 *   ;;; file descriptor not currently open; it is randomized to prevent
 *   ;;; applications from depending on making assumptions about indexes, since this
 *   ;;; is error-prone in multi-threaded contexts. The returned file descriptor is
 *   ;;; guaranteed to be less than 2**31.
 *   ;;
 *   ;;; Note: This is similar to `openat` in POSIX.
 *   (@interface func (export "path_open")
 *     (param $fd $fd)
 *     ;;; Flags determining the method of how the path is resolved.
 *     (param $dirflags $lookupflags)
 *     ;;; The relative path of the file or directory to open, relative to the
 *     ;;; `path_open::fd` directory.
 *     (param $path string)
 *     ;;; The method by which to open the file.
 *     (param $oflags $oflags)
 *     ;;; The initial rights of the newly created file descriptor. The
 *     ;;; implementation is allowed to return a file descriptor with fewer rights
 *     ;;; than specified, if and only if those rights do not apply to the type of
 *     ;;; file being opened.
 *     ;;
 *     ;;; The *base* rights are rights that will apply to operations using the file
 *     ;;; descriptor itself, while the *inheriting* rights are rights that apply to
 *     ;;; file descriptors derived from it.
 *     (param $fs_rights_base $rights)
 *     (param $fs_rights_inheriting $rights)
 *     (param $fdflags $fdflags)
 *     ;;; The file descriptor of the file that has been opened.
 *     (result $error (expected $fd (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "path_open")
internal external fun wasiPathOpen(
    fd: WasiHandle,
    dirflags: LookupflagsWasmType,
    path: WasmPtr,
    pathSize: Int,
    oflags: OflagsWasmType,
    fsRightsBase: RightsWasmType,
    fsRightsInheriting: RightsWasmType,
    fdflags: FdflagsWasmType,
    expectedFd: WasmPtr,
): Int

/**
 * WASI Preview1 function `path_remove_directory`
 *
 * Definition:
 * ```
 * ;;; Remove a directory.
 *   ;;; Return `errno::notempty` if the directory is not empty.
 *   ;;; Note: This is similar to `unlinkat(fd, path, AT_REMOVEDIR)` in POSIX.
 *   (@interface func (export "path_remove_directory")
 *     (param $fd $fd)
 *     ;;; The path to a directory to remove.
 *     (param $path string)
 *     (result $error (expected (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "path_remove_directory")
internal external fun wasiPathRemoveDirectory(fd: WasiHandle, path: WasmPtr, pathSize: Int): Int

/**
 * WASI Preview1 function `path_unlink_file`
 *
 * Definition:
 * ```
 * ;;; Unlink a file.
 *   ;;; Return `errno::isdir` if the path refers to a directory.
 *   ;;; Note: This is similar to `unlinkat(fd, path, 0)` in POSIX.
 *   (@interface func (export "path_unlink_file")
 *     (param $fd $fd)
 *     ;;; The path to a file to unlink.
 *     (param $path string)
 *     (result $error (expected (error $errno)))
 *   )
 * ```
 */
@WasmImport("wasi_snapshot_preview1", "path_unlink_file")
internal external fun wasiPathUnlinkFile(fd: WasiHandle, path: WasmPtr, pathSize: Int): Int
