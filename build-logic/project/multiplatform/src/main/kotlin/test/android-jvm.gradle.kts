/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.test

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * Convention plugin that configures android unit tests
 */
plugins.withId("com.android.kotlin.multiplatform.library") {
    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
            withHostTest {
                isReturnDefaultValues = false
                isIncludeAndroidResources = false
            }
        }
    }

    extensions.configure<KotlinMultiplatformAndroidComponentsExtension> {
        onVariants { variant ->
            variant.hostTests.values.forEach { hostTest ->
                val prepareTempTask = PrepareTempRootTask.setup(hostTest.name, tasks, layout)
                hostTest.configureTestTask { testTask ->
                    val tempRoot = prepareTempTask.flatMap(PrepareTempRootTask::outputDirectory)
                        .get().asFile.absolutePath
                    configureTestTaskDefaults(testTask)
                    testTask.systemProperty("java.io.tmpdir", tempRoot)
                    testTask.environment(ENV_TEST_TMP_DIR, tempRoot)
                    testTask.dependsOn(prepareTempTask)
                }
            }
        }
    }
}
