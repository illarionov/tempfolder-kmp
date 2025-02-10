/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

// Keep in sync with build-logic/project/multiplatform/src/main/kotlin/test/TestExt.kt
internal const val ENV_TEST_TMP_DIR = "TEST_TMP_DIR"

internal expect val testTempDirRoot: String
