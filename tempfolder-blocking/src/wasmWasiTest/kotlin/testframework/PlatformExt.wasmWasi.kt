/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import at.released.tempfolder.wasip1.wasiLoadEnv
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.LazyThreadSafetyMode.PUBLICATION

// Keep in sync with build-logic/project/multiplatform/src/main/kotlin/test/TestExt.kt
internal const val ENV_IS_NODE_ON_WINDOWS = "IS_NODE_ON_WINDOWS"

internal actual val testTempDirRoot: String by lazy(NONE) {
    wasiLoadEnv(ENV_TEST_TMP_DIR) ?: error("$ENV_TEST_TMP_DIR not defined")
}

private val isNodeOnWindows: Boolean by lazy(PUBLICATION) {
    wasiLoadEnv(ENV_IS_NODE_ON_WINDOWS)?.toBoolean() ?: false
}

actual fun isSimulatorOrVirtualDevice(): Boolean = false

// fd_readdir is not implemented on NodeJS for Windows
// https://github.com/nodejs/uvwasi/blob/6eeddbae277693bc022e59e54649ec13eed478c7/src/uvwasi.c#L1537
actual fun isReadingDirectorySupported(): Boolean = !isNodeOnWindows
