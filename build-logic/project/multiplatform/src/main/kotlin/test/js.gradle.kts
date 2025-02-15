/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.test

import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

/*
 * Convention plugin that configures Node JS tests in Kotlin Multiplatform project
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType<KotlinJsIrTarget>().all {
            nodejs {
                testRuns.configureEach {
                    val prepareTempRootTask = PrepareTempRootTask.setup(this, tasks, layout)
                    val tempRoot = prepareTempRootTask.flatMap(PrepareTempRootTask::outputDirectory)
                        .get().asFile.absolutePath
                    executionTask.configure {
                        environment("TMPDIR", tempRoot)
                        environment("TEMP", tempRoot)
                        environment(ENV_TEST_TMP_DIR, tempRoot)
                        dependsOn(prepareTempRootTask)
                    }
                }
            }
        }
    }
}
