/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.distribution

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

abstract class CleanupDirectoryTask : DefaultTask() {
    @get:InputFiles
    abstract val inputDirectory: DirectoryProperty

    @TaskAction
    fun cleanup() {
        val dir = inputDirectory.get().asFile
        if (dir.isDirectory) {
            dir.deleteRecursively()
        }
    }
}
