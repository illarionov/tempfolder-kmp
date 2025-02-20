/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

internal actual val testTempDirRoot: String
    get() = js("globalThis.process.env[\"$ENV_TEST_TMP_DIR\"]") as String? ?: error("$ENV_TEST_TMP_DIR not defined")

internal actual fun isSimulatorOrVirtualDevice(): Boolean = false

internal actual fun isReadingDirectorySupported(): Boolean = true
