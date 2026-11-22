/*
 * SPDX-FileCopyrightText: 2024-2026 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.lint

import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

/*
 * Convention plugin that configures Kotlin's built-in ABI validator for the library's public API.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

@OptIn(ExperimentalAbiValidation::class)
kotlin {
    abiValidation {
        filters.exclude {
            annotatedWith.add("at.released.tempfolder.InternalTempfolderApi")
        }
    }
}
