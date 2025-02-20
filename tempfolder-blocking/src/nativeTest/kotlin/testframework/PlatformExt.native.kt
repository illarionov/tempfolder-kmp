/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import kotlinx.cinterop.toKString
import platform.posix.getenv

internal actual val testTempDirRoot: String
    get() = getenv(ENV_TEST_TMP_DIR)?.toKString() ?: error("$ENV_TEST_TMP_DIR environment variable not set")

actual fun isReadingDirectorySupported(): Boolean = true
