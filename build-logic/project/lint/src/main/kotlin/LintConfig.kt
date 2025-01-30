/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.lint

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternFilterable

internal val Project.configRootDir: Directory
    get() {
        return rootProject.layout.projectDirectory.dir("config")
    }

internal val Project.lintedFileTree: FileTree
    get() = rootProject.layout.projectDirectory.asFileTree.matching {
        excludeNonLintedDirectories()
    }

internal fun PatternFilterable.excludeNonLintedDirectories() {
    exclude {
        it.isDirectory && it.name in excludedDirectories
    }
    exclude {
        it.isDirectory && it.relativePath.startsWith("config/copyright")
    }
    exclude {
        it.isDirectory && it.relativePath.startsWith("test-wasi-testsuite/wasi-testsuite")
    }
    exclude("**/api/**/*.api")
}

private val excludedDirectories = setOf(
    ".git",
    ".gradle",
    ".idea",
    "build",
    "generated",
    "node_modules",
    "out",
)
