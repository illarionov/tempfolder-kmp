/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.test

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.ExecutionTaskHolder
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetTestRun
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.Family.ANDROID
import org.jetbrains.kotlin.konan.target.Family.MINGW
import org.jetbrains.kotlin.konan.target.KonanTarget

/*
 * Convention plugin that configures tests for wasmWasi target in projects with the Kotlin Multiplatform plugin
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
    val konanTarget: KonanTarget = (this.target as KotlinNativeTargetWithTests<*>).konanTarget

    val prepareTempRootTask = PrepareTempRootTask.setup(this, tasks, layout)
    @Suppress("UNCHECKED_CAST")
    (this as ExecutionTaskHolder<KotlinNativeTest>).executionTask.configure {
        val tempRoot = prepareTempRootTask.flatMap(PrepareTempRootTask::outputDirectory).get().asFile.absolutePath
        environment(ENV_TEST_TMP_DIR, tempRoot)
        environment("SIMCTL_CHILD_$ENV_TEST_TMP_DIR", tempRoot) // sets variable in IOS simulator

        if (konanTarget.family == MINGW) {
            environment("TMP", tempRoot)
        } else {
            environment("TMPDIR", tempRoot)
            environment("SIMCTL_CHILD_TMPDIR", tempRoot)
        }

        dependsOn(prepareTempRootTask)
    }
}
