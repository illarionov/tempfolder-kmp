/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

internal actual val testTempDirRoot: String
    get() = System.getenv(ENV_TEST_TMP_DIR) ?: error("$ENV_TEST_TMP_DIR environment variable not set")

actual fun isSimulatorOrVirtualDevice(): Boolean = false
actual fun isReadingDirectorySupported(): Boolean = true
