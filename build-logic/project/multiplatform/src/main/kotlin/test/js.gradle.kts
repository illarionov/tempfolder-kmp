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
 * Convention plugin that configures Node JS tests in Kotlin Multiplatform project
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType<KotlinJsIrTarget>().all {
            nodejs {
                testRuns.configureEach {
                    executionTask {
                        nodeJsArgs += "--experimental-wasm-exnref"
                    }
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
    if (target.name == "wasmWasi") {
        val driverFile = layout.buildDirectory.file(
            "compileSync/wasmWasi/test/testDevelopmentExecutable/kotlin/" +
                    "tempfolder-kmp-tempfolder-blocking-wasm-wasi-test.mjs",
        )
        executionTask {
            doFirst(ModifyWasiDriverAction(driverFile, tempRoot))
        }
    }
}

// Workaround https://youtrack.jetbrains.com/issue/KT-65179
class ModifyWasiDriverAction(
    private val driverFile: Provider<RegularFile>,
    private val tempRoot: String,
) : Action<Task> {
    override fun execute(task: Task) {
        val driverContent = driverFile.get().asFile.readText()
        val newContent = driverContent.replace(
            "const wasi = new WASI({ version: 'preview1', args: argv, env, });",
            "const wasi = new WASI({ version: 'preview1', args: argv, env, preopens: { '$tempRoot': '$tempRoot' }});",
        )
        driverFile.get().asFile.writeText(newContent)
    }
}
