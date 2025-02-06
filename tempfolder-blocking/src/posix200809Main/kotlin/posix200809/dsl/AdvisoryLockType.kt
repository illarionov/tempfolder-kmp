/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.posix200809.dsl

/**
 * The type of advisory lock to be set on an open temporarily directory.
 *
 * Used to protect a directory from being deleted by automatic clean-up system such as systemd-tmpfiles.
 */
public enum class AdvisoryLockType {
    NONE,
    EXCLUSIVE,
    SHARED,
}
