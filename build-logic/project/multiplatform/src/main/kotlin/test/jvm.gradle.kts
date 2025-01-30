/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.test

/*
 * Convention plugin that configures unit tests in projects with the Kotlin Multiplatform plugin
 */
tasks.withType<Test> {
    configureTestTaskDefaults(this)
}
