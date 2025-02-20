/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SpacingBetweenPackageAndImports")

package at.released.tempfolder.gradle.multiplatform.test

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsPlatformTestRun
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

/*
 * Convention plugin that configures Node JS tests in Kotlin Multiplatform project
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        targets.matching { it.name == "wasmWasi" }.withType<KotlinJsIrTarget> {
            nodejs {
                testRuns.configureEach {
                    setupNodeWasiTestRun()
                }
            }
        }
    }
}

private fun KotlinJsPlatformTestRun.setupNodeWasiTestRun() {
    val prepareTempRootTask = PrepareTempRootTask.setup(this, tasks, layout)
    val tempRoot: String = prepareTempRootTask.flatMap(PrepareTempRootTask::outputDirectory)
        .get().asFile.absolutePath
    val virtualTempRoot = "/mytemp"
    val isWindows = Os.isFamily("windows")
    executionTask {
        environment("TMPDIR", virtualTempRoot)
        environment("TEMP", virtualTempRoot)
        environment(ENV_TEST_TMP_DIR, virtualTempRoot)
        environment(ENV_IS_NODE_ON_WINDOWS, isWindows.toString())
        dependsOn(prepareTempRootTask)
    }
    val driverFile = layout.buildDirectory.file(
        "compileSync/wasmWasi/test/testDevelopmentExecutable/kotlin/" +
                "tempfolder-kmp-tempfolder-sync-wasm-wasi-test.mjs",
    )
    executionTask {
        doFirst(ModifyWasiDriverAction(driverFile, virtualTempRoot, tempRoot))
    }
}

// Workaround https://youtrack.jetbrains.com/issue/KT-65179
class ModifyWasiDriverAction(
    private val driverFile: Provider<RegularFile>,
    private val virtualTempRoot: String,
    private val realTempRoot: String,
) : Action<Task> {
    override fun execute(task: Task) {
        val nodeTempRoot = realTempRoot.replace('\\', '/') // NodeJs on Windows works only with slash.
        val driverContent = driverFile.get().asFile.readText()
        val newContent = driverContent.replace(
            "const wasi = new WASI({ version: 'preview1', args: argv, env, });",
            "const wasi = new WASI({ version: 'preview1', args: argv, env, " +
                    "preopens: { '$virtualTempRoot': '$nodeTempRoot' }});",
        )
        driverFile.get().asFile.writeText(newContent)
    }
}
