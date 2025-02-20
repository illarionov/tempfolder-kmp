/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.dsl

/**
 * Estimated size of files in the temporary directory.
 *
 * Used to determine the preferred temporary directory on Unix-like operating systems if the TEMPDIR environment
 * variable is not set.
 */
public enum class TempfolderSizeEstimate {
    /**
     * Small directory size. The `/tmp` directory will be preferred.
     */
    SMALL,

    /**
     * Large directory size. The `/var/tmp` directory is preferred.
     */
    LARGE,
}
