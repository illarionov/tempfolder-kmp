/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MaxLineLength")

package at.released.tempfolder.testframework.wasip1.type

import at.released.tempfolder.wasip1.type.Filetype
import at.released.tempfolder.wasip1.type.Inode

/**
 * WASI Preview1 type `$filestat`
 *
 * File attributes.
 *
 * @param dev Device ID of device containing the file.
 * @param ino File serial number.
 * @param filetype File type.
 * @param nlink Number of hard links to the file.
 * @param size For regular files, the file size in bytes. For symbolic links, the length in bytes of the pathname contained in the symbolic link.
 * @param atim Last data access timestamp. This can be 0 if the underlying platform doesn't provide suitable timestamp for this file.
 * @param mtim Last data modification timestamp. This can be 0 if the underlying platform doesn't provide suitable timestamp for this file.
 * @param ctim Last file status change timestamp. This can be 0 if the underlying platform doesn't provide suitable timestamp for this file.
 *
 * Definition:
 * ```
 * (typename $filestat
 *   (record
 *     ;;; Device ID of device containing the file.
 *     (field $dev $device)
 *     ;;; File serial number.
 *     (field $ino $inode)
 *     ;;; File type.
 *     (field $filetype $filetype)
 *     ;;; Number of hard links to the file.
 *     (field $nlink $linkcount)
 *     ;;; For regular files, the file size in bytes. For symbolic links, the length in bytes of the pathname contained in the symbolic link.
 *     (field $size $filesize)
 *     ;;; Last data access timestamp.
 *     ;;; This can be 0 if the underlying platform doesn't provide suitable
 *     ;;; timestamp for this file.
 *     (field $atim $timestamp)
 *     ;;; Last data modification timestamp.
 *     ;;; This can be 0 if the underlying platform doesn't provide suitable
 *     ;;; timestamp for this file.
 *     (field $mtim $timestamp)
 *     ;;; Last file status change timestamp.
 *     ;;; This can be 0 if the underlying platform doesn't provide suitable
 *     ;;; timestamp for this file.
 *     (field $ctim $timestamp)
 *   )
 * )
 * ```
 */
internal data class Filestat(
    val dev: Device,
    val ino: Inode,
    val filetype: Filetype,
    val nlink: Linkcount,
    val size: Filesize,
    val atim: Timestamp,
    val mtim: Timestamp,
    val ctim: Timestamp,
) {
    companion object
}
