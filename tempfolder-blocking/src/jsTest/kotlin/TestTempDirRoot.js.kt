/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

internal actual val testTempDirRoot: String
    get() = js("globalThis.process.env[\"$ENV_TEST_TMP_DIR\"]") as String? ?: error("$ENV_TEST_TMP_DIR not defined")
