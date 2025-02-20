/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.wasip1.type

/**
 * WASI Preview1 type `$filetype`
 *
 * The type of a file descriptor or file.
 *
 * TAG: `u8`
 *
 * Definition:
 * ```
 * (typename $filetype
 *   (enum (@witx tag u8)
 *     ;;; The type of the file descriptor or file is unknown or is different from any of the other types specified.
 *     $unknown
 *     ;;; The file descriptor or file refers to a block device inode.
 *     $block_device
 *     ;;; The file descriptor or file refers to a character device inode.
 *     $character_device
 *     ;;; The file descriptor or file refers to a directory inode.
 *     $directory
 *     ;;; The file descriptor or file refers to a regular file inode.
 *     $regular_file
 *     ;;; The file descriptor or file refers to a datagram socket.
 *     $socket_dgram
 *     ;;; The file descriptor or file refers to a byte-stream socket.
 *     $socket_stream
 *     ;;; The file refers to a symbolic link inode.
 *     $symbolic_link
 *   )
 * )
 * ```
 */
internal enum class Filetype {
    /**
     * The type of the file descriptor or file is unknown or is different from any of the other types specified.
     */
    UNKNOWN,

    /**
     * The file descriptor or file refers to a block device inode.
     */
    BLOCK_DEVICE,

    /**
     * The file descriptor or file refers to a character device inode.
     */
    CHARACTER_DEVICE,

    /**
     * The file descriptor or file refers to a directory inode.
     */
    DIRECTORY,

    /**
     * The file descriptor or file refers to a regular file inode.
     */
    REGULAR_FILE,

    /**
     * The file descriptor or file refers to a datagram socket.
     */
    SOCKET_DGRAM,

    /**
     * The file descriptor or file refers to a byte-stream socket.
     */
    SOCKET_STREAM,

    /**
     * The file refers to a symbolic link inode.
     */
    SYMBOLIC_LINK,
    ;

    val code: Int
        get() = ordinal

    companion object {
        fun fromCode(code: Int): Filetype? = entries.getOrNull(code)
    }
}
