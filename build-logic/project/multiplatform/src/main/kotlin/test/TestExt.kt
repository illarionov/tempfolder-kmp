/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.test

// Keep in sync with tempfolder-sync/src/commonTest/kotlin/TestTempDirRoot.kt
internal const val ENV_TEST_TMP_DIR = "TEST_TMP_DIR"

// Keep in sync with tempfolder-sync/src/wasmWasiTest/kotlin/testframework/PlatformExt.wasmWasi.kt
internal const val ENV_IS_NODE_ON_WINDOWS = "IS_NODE_ON_WINDOWS"
