/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809

import at.released.tempfolder.platform.linux.AT_FDCWD

internal val TempfolderPosixFileDescriptor.linuxFd: Int
    get() = if (this == TempfolderPosixFileDescriptor.CURRENT_WORKING_DIRECTORY) {
        AT_FDCWD
    } else {
        this.fd
    }
