/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MaxLineLength", "LONG_NUMERICAL_VALUES_SEPARATED")

package at.released.tempfolder.wasip1.type

import at.released.tempfolder.wasip1.WasiU64

/**
 * WASI Preview1 type `$rights`
 *
 * File descriptor rights, determining which actions may be performed.
 *
 * Representation: `u64`
 *
 * Definition:
 * ```
 * (typename $rights
 *   (flags (@witx repr u64)
 *     ;;; The right to invoke `fd_datasync`.
 *     ;;
 *     ;;; If `path_open` is set, includes the right to invoke
 *     ;;; `path_open` with `fdflags::dsync`.
 *     $fd_datasync
 *     ;;; The right to invoke `fd_read` and `sock_recv`.
 *     ;;
 *     ;;; If `rights::fd_seek` is set, includes the right to invoke `fd_pread`.
 *     $fd_read
 *     ;;; The right to invoke `fd_seek`. This flag implies `rights::fd_tell`.
 *     $fd_seek
 *     ;;; The right to invoke `fd_fdstat_set_flags`.
 *     $fd_fdstat_set_flags
 *     ;;; The right to invoke `fd_sync`.
 *     ;;
 *     ;;; If `path_open` is set, includes the right to invoke
 *     ;;; `path_open` with `fdflags::rsync` and `fdflags::dsync`.
 *     $fd_sync
 *     ;;; The right to invoke `fd_seek` in such a way that the file offset
 *     ;;; remains unaltered (i.e., `whence::cur` with offset zero), or to
 *     ;;; invoke `fd_tell`.
 *     $fd_tell
 *     ;;; The right to invoke `fd_write` and `sock_send`.
 *     ;;; If `rights::fd_seek` is set, includes the right to invoke `fd_pwrite`.
 *     $fd_write
 *     ;;; The right to invoke `fd_advise`.
 *     $fd_advise
 *     ;;; The right to invoke `fd_allocate`.
 *     $fd_allocate
 *     ;;; The right to invoke `path_create_directory`.
 *     $path_create_directory
 *     ;;; If `path_open` is set, the right to invoke `path_open` with `oflags::creat`.
 *     $path_create_file
 *     ;;; The right to invoke `path_link` with the file descriptor as the
 *     ;;; source directory.
 *     $path_link_source
 *     ;;; The right to invoke `path_link` with the file descriptor as the
 *     ;;; target directory.
 *     $path_link_target
 *     ;;; The right to invoke `path_open`.
 *     $path_open
 *     ;;; The right to invoke `fd_readdir`.
 *     $fd_readdir
 *     ;;; The right to invoke `path_readlink`.
 *     $path_readlink
 *     ;;; The right to invoke `path_rename` with the file descriptor as the source directory.
 *     $path_rename_source
 *     ;;; The right to invoke `path_rename` with the file descriptor as the target directory.
 *     $path_rename_target
 *     ;;; The right to invoke `path_filestat_get`.
 *     $path_filestat_get
 *     ;;; The right to change a file's size.
 *     ;;; If `path_open` is set, includes the right to invoke `path_open` with `oflags::trunc`.
 *     ;;; Note: there is no function named `path_filestat_set_size`. This follows POSIX design,
 *     ;;; which only has `ftruncate` and does not provide `ftruncateat`.
 *     ;;; While such function would be desirable from the API design perspective, there are virtually
 *     ;;; no use cases for it since no code written for POSIX systems would use it.
 *     ;;; Moreover, implementing it would require multiple syscalls, leading to inferior performance.
 *     $path_filestat_set_size
 *     ;;; The right to invoke `path_filestat_set_times`.
 *     $path_filestat_set_times
 *     ;;; The right to invoke `fd_filestat_get`.
 *     $fd_filestat_get
 *     ;;; The right to invoke `fd_filestat_set_size`.
 *     $fd_filestat_set_size
 *     ;;; The right to invoke `fd_filestat_set_times`.
 *     $fd_filestat_set_times
 *     ;;; The right to invoke `path_symlink`.
 *     $path_symlink
 *     ;;; The right to invoke `path_remove_directory`.
 *     $path_remove_directory
 *     ;;; The right to invoke `path_unlink_file`.
 *     $path_unlink_file
 *     ;;; If `rights::fd_read` is set, includes the right to invoke `poll_oneoff` to subscribe to `eventtype::fd_read`.
 *     ;;; If `rights::fd_write` is set, includes the right to invoke `poll_oneoff` to subscribe to `eventtype::fd_write`.
 *     $poll_fd_readwrite
 *     ;;; The right to invoke `sock_shutdown`.
 *     $sock_shutdown
 *     ;;; The right to invoke `sock_accept`.
 *     $sock_accept
 *   )
 * )
 * ```
 */
internal typealias Rights = Long

internal typealias RightsWasmType = WasiU64

internal object RightsFlag {
    /**
     * The right to invoke `fd_datasync`.  If `path_open` is set, includes the right to invoke `path_open` with `fdflags::dsync`.
     */
    val FD_DATASYNC: ULong = 0x01.toULong()

    /**
     * The right to invoke `fd_read` and `sock_recv`.  If `rights::fd_seek` is set, includes the right to invoke `fd_pread`.
     */
    val FD_READ: ULong = 0x02.toULong()

    /**
     * The right to invoke `fd_seek`. This flag implies `rights::fd_tell`.
     */
    val FD_SEEK: ULong = 0x04.toULong()

    /**
     * The right to invoke `fd_fdstat_set_flags`.
     */
    val FD_FDSTAT_SET_FLAGS: ULong = 0x08.toULong()

    /**
     * The right to invoke `fd_sync`.  If `path_open` is set, includes the right to invoke `path_open` with `fdflags::rsync` and `fdflags::dsync`.
     */
    val FD_SYNC: ULong = 0x10.toULong()

    /**
     * The right to invoke `fd_seek` in such a way that the file offset remains unaltered (i.e., `whence::cur` with offset zero), or to invoke `fd_tell`.
     */
    val FD_TELL: ULong = 0x20.toULong()

    /**
     * The right to invoke `fd_write` and `sock_send`. If `rights::fd_seek` is set, includes the right to invoke `fd_pwrite`.
     */
    val FD_WRITE: ULong = 0x40.toULong()

    /**
     * The right to invoke `fd_advise`.
     */
    val FD_ADVISE: ULong = 0x80.toULong()

    /**
     * The right to invoke `fd_allocate`.
     */
    val FD_ALLOCATE: ULong = 0x100.toULong()

    /**
     * The right to invoke `path_create_directory`.
     */
    val PATH_CREATE_DIRECTORY: ULong = 0x200.toULong()

    /**
     * If `path_open` is set, the right to invoke `path_open` with `oflags::creat`.
     */
    val PATH_CREATE_FILE: ULong = 0x400.toULong()

    /**
     * The right to invoke `path_link` with the file descriptor as the source directory.
     */
    val PATH_LINK_SOURCE: ULong = 0x800.toULong()

    /**
     * The right to invoke `path_link` with the file descriptor as the target directory.
     */
    val PATH_LINK_TARGET: ULong = 0x1000.toULong()

    /**
     * The right to invoke `path_open`.
     */
    val PATH_OPEN: ULong = 0x2000.toULong()

    /**
     * The right to invoke `fd_readdir`.
     */
    val FD_READDIR: ULong = 0x4000.toULong()

    /**
     * The right to invoke `path_readlink`.
     */
    val PATH_READLINK: ULong = 0x8000.toULong()

    /**
     * The right to invoke `path_rename` with the file descriptor as the source directory.
     */
    val PATH_RENAME_SOURCE: ULong = 0x10000.toULong()

    /**
     * The right to invoke `path_rename` with the file descriptor as the target directory.
     */
    val PATH_RENAME_TARGET: ULong = 0x20000.toULong()

    /**
     * The right to invoke `path_filestat_get`.
     */
    val PATH_FILESTAT_GET: ULong = 0x40000.toULong()

    /**
     * The right to change a file's size. If `path_open` is set, includes the right to invoke `path_open` with `oflags::trunc`. Note: there is no function named `path_filestat_set_size`. This follows POSIX design, which only has `ftruncate` and does not provide `ftruncateat`. While such function would be desirable from the API design perspective, there are virtually no use cases for it since no code written for POSIX systems would use it. Moreover, implementing it would require multiple syscalls, leading to inferior performance.
     */
    val PATH_FILESTAT_SET_SIZE: ULong = 0x80000.toULong()

    /**
     * The right to invoke `path_filestat_set_times`.
     */
    val PATH_FILESTAT_SET_TIMES: ULong = 0x100000.toULong()

    /**
     * The right to invoke `fd_filestat_get`.
     */
    val FD_FILESTAT_GET: ULong = 0x200000.toULong()

    /**
     * The right to invoke `fd_filestat_set_size`.
     */
    val FD_FILESTAT_SET_SIZE: ULong = 0x400000.toULong()

    /**
     * The right to invoke `fd_filestat_set_times`.
     */
    val FD_FILESTAT_SET_TIMES: ULong = 0x800000.toULong()

    /**
     * The right to invoke `path_symlink`.
     */
    val PATH_SYMLINK: ULong = 0x1000000.toULong()

    /**
     * The right to invoke `path_remove_directory`.
     */
    val PATH_REMOVE_DIRECTORY: ULong = 0x2000000.toULong()

    /**
     * The right to invoke `path_unlink_file`.
     */
    val PATH_UNLINK_FILE: ULong = 0x4000000.toULong()

    /**
     * If `rights::fd_read` is set, includes the right to invoke `poll_oneoff` to subscribe to `eventtype::fd_read`. If `rights::fd_write` is set, includes the right to invoke `poll_oneoff` to subscribe to `eventtype::fd_write`.
     */
    val POLL_FD_READWRITE: ULong = 0x8000000.toULong()

    /**
     * The right to invoke `sock_shutdown`.
     */
    val SOCK_SHUTDOWN: ULong = 0x10000000.toULong()

    /**
     * The right to invoke `sock_accept`.
     */
    val SOCK_ACCEPT: ULong = 0x20000000.toULong()
    val DIRECTORY_BASE_RIGHTS: ULong = PATH_CREATE_DIRECTORY or
                PATH_CREATE_FILE or
                PATH_LINK_SOURCE or
                PATH_LINK_TARGET or
                PATH_OPEN or
                FD_READDIR or
                PATH_READLINK or
                PATH_RENAME_SOURCE or
                PATH_RENAME_TARGET or
                PATH_SYMLINK or
                PATH_REMOVE_DIRECTORY or
                PATH_UNLINK_FILE or
                PATH_FILESTAT_GET or
                PATH_FILESTAT_SET_TIMES or
                FD_FILESTAT_GET or
                FD_FILESTAT_SET_TIMES
    val FILE_BASE_RIGHTS: ULong = FD_DATASYNC or
            FD_READ or
            FD_SEEK or
            FD_FDSTAT_SET_FLAGS or
            FD_SYNC or
            FD_TELL or
            FD_WRITE or
            FD_ADVISE or
            FD_ALLOCATE or
            FD_FILESTAT_GET or
            FD_FILESTAT_SET_SIZE or
            FD_FILESTAT_SET_TIMES or
            POLL_FD_READWRITE
    val DIRECTORY_INHERITING_RIGHTS: ULong = DIRECTORY_BASE_RIGHTS or DIRECTORY_BASE_RIGHTS
}
