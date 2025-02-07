/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.PosixPathString

@Throws(TempfolderIOException::class)
internal expect fun isDirectory(dirFd: TempfolderPosixFileDescriptor, name: PosixPathString): Boolean
