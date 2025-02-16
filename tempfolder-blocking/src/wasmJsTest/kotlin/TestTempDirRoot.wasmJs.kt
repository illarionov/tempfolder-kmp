/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

internal actual val testTempDirRoot: String get() = getEnvTempDir(ENV_TEST_TMP_DIR)

@Suppress("UnusedParameter")
private fun getEnvTempDir(envName: String): String = js("globalThis.process.env[envName]")
