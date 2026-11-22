/*
 * SPDX-FileCopyrightText: 2024-2026 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.lint

import com.android.build.api.dsl.Lint

/**
 * Android Lint defaults
 */
internal fun Lint.configureCommonAndroidLint() {
    quiet = false
    ignoreWarnings = false
    checkDependencies = false
    ignoreTestSources = false

    disable += "ObsoleteSdkInt"
    informational += "GradleDependency"
}
