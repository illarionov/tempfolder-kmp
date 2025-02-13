/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.test

import at.released.tempfolder.gradle.multiplatform.ext.capitalizeAscii
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetTestRun

abstract class PrepareTempRootTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun execute() {
        val path = outputDirectory.get().asFile
        path.mkdirs()
        path.walkBottomUp()
            .filter { it != path }
            .forEach {
                it.delete()
            }
    }

    public companion object {
        fun setup(
            targetTestRun: KotlinTargetTestRun<*>,
            tasks: TaskContainer,
            layout: ProjectLayout,
        ): TaskProvider<PrepareTempRootTask> {
            val targetName = targetTestRun.target.disambiguationClassifier ?: error("No disambiguationClassifier")
            val testRunFullName = "$targetName${targetTestRun.name.capitalizeAscii()}"
            return setup(testRunFullName, tasks, layout)
        }

        fun setup(
            tempDirectoryName: String,
            tasks: TaskContainer,
            layout: ProjectLayout,
        ): TaskProvider<PrepareTempRootTask> {
            @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
            return tasks.register<PrepareTempRootTask>(
                "prepareTempRoot${tempDirectoryName.capitalizeAscii()}",
            ) {
                this.outputDirectory.set(layout.buildDirectory.dir("testTemp/$tempDirectoryName"))
            }
        }
    }
}
