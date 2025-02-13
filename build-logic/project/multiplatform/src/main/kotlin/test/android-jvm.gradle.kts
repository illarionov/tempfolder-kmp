/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.test

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryPlugin

/*
 * Convention plugin that configures android unit tests
 */
plugins.withType(LibraryPlugin::class.java) {
    extensions.configure(CommonExtension::class.java) {
        val testDebugTempDirTask: TaskProvider<PrepareTempRootTask> =
            PrepareTempRootTask.setup("androidTestDebug", tasks, layout)
        val testReleaseTempDirTask = PrepareTempRootTask.setup("androidTestRelease", tasks, layout)

        @Suppress("UnstableApiUsage")
        testOptions {
            unitTests {
                isReturnDefaultValues = false
                isIncludeAndroidResources = false
                all { testTask: Test ->
                    configureTestTaskDefaults(testTask)
                    val prepareTempTask = when (testTask.name) {
                        "testDebugUnitTest" -> testDebugTempDirTask
                        "testReleaseUnitTest" -> testReleaseTempDirTask
                        else -> error("Unexpected test task ${testTask.name}")
                    }
                    val tempRoot = prepareTempTask.flatMap(PrepareTempRootTask::outputDirectory)
                        .get().asFile.absolutePath
                    testTask.systemProperty("java.io.tmpdir", tempRoot)
                    testTask.environment(ENV_TEST_TMP_DIR, tempRoot)
                    testTask.dependsOn(prepareTempTask)
                }
            }
        }
    }
}
