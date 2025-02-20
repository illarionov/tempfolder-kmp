/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SpacingBetweenPackageAndImports")

package at.released.tempfolder.gradle.multiplatform.test

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsPlatformTestRun
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

/*
 * Convention plugin that configures Node JS tests for JS target in Kotlin Multiplatform project
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        targets.matching { it.name == "js" }.withType<KotlinJsIrTarget> {
            nodejs {
                testRuns.configureEach {
                    setupNodejsTestRun()
                }
            }
        }
    }
}

private fun KotlinJsPlatformTestRun.setupNodejsTestRun() {
    val prepareTempRootTask = PrepareTempRootTask.setup(this, tasks, layout)
    val tempRoot: String = prepareTempRootTask.flatMap(PrepareTempRootTask::outputDirectory)
        .get().asFile.absolutePath
    executionTask {
        environment("TMPDIR", tempRoot)
        environment("TEMP", tempRoot)
        environment(ENV_TEST_TMP_DIR, tempRoot)
        dependsOn(prepareTempRootTask)
    }
}
