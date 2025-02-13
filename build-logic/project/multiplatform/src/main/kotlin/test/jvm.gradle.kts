/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.test

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/*
 * Convention plugin that configures unit tests in projects with the Kotlin Multiplatform plugin
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        targets
            .withType<KotlinJvmTarget>()
            .configureEach {
                testRuns.configureEach {
                    val prepareTempRootTask = PrepareTempRootTask.setup(this, tasks, layout)
                    val tempRoot = prepareTempRootTask.flatMap(PrepareTempRootTask::outputDirectory).get().asFile.absolutePath
                    executionTask.configure {
                        systemProperty("java.io.tmpdir", tempRoot)
                        environment(ENV_TEST_TMP_DIR, tempRoot)
                        dependsOn(prepareTempRootTask)
                        configureTestTaskDefaults(this)
                    }
                }
            }
    }
}
