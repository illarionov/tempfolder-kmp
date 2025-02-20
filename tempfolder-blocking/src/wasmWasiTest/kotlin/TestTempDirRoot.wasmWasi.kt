/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

import at.released.tempfolder.wasip1.wasiLoadEnv
import kotlin.LazyThreadSafetyMode.NONE

internal actual val testTempDirRoot: String by lazy(NONE) {
    wasiLoadEnv(ENV_TEST_TMP_DIR) ?: error("$ENV_TEST_TMP_DIR not defined")
}
