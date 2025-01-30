/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform

/*
 * Convention plugin that configures Atomicfu
 */
plugins {
    id("org.jetbrains.kotlinx.atomicfu")
}

atomicfu {
    transformJvm = true
    jvmVariant = "FU"
}
