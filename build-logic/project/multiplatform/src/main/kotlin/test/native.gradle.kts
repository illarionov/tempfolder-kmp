/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.test

import at.released.tempfolder.gradle.multiplatform.ext.capitalizeAscii
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.ExecutionTaskHolder
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetTestRun
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.Family.ANDROID

/*
 * Convention plugin tests for native targets in projects with the Kotlin Multiplatform plugin
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        targets
            .withType<KotlinNativeTarget>()
            .matching { it is KotlinNativeTargetWithTests<*> && it.konanTarget.family != ANDROID }
            .configureEach {
                (this as KotlinNativeTargetWithTests<*>).testRuns.configureEach {
                    setupTestTmpDirectory()
                }
            }
    }
}

fun KotlinTargetTestRun<*>.setupTestTmpDirectory() {
    val prepareTempRootTask = PrepareTempRootTask.setup(this, tasks, layout)
    @Suppress("UNCHECKED_CAST")
    (this as ExecutionTaskHolder<KotlinNativeTest>).executionTask.configure {
        val tempRoot = prepareTempRootTask.flatMap(PrepareTempRootTask::outputDirectory).get().asFile.absolutePath
        environment("TMPDIR", tempRoot)
        environment(ENV_TEST_TMP_DIR, tempRoot)
        dependsOn(prepareTempRootTask)
    }
}
