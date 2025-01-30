/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.lint

/*
 * Convention plugin that configures Spotless
 */
plugins {
    id("com.diffplug.spotless")
}

spotless {
    isEnforceCheck = false

    val rootDir = lintedFileTree

    format("properties") {
        // "**/.gitignore" does not work: https://github.com/diffplug/spotless/issues/1146
        val propertyFiles = setOf(
            ".gitignore",
            ".gitattributes",
            ".editorconfig",
            "gradle.properties",
        )
        target(rootDir.filter { it.name in propertyFiles })

        trimTrailingWhitespace()
        endWithNewline()
    }
    format("yaml") {
        target(rootDir.filter { it.name.endsWith(".yml") })

        trimTrailingWhitespace()
        endWithNewline()
        leadingTabsToSpaces(2)
    }
    format("toml") {
        target(rootDir.filter { it.name.endsWith(".toml") })

        trimTrailingWhitespace()
        endWithNewline()
        leadingTabsToSpaces(2)
    }
    format("markdown") {
        target(rootDir.filter { it.name.endsWith(".md") || it.name.endsWith(".markdown") })

        endWithNewline()
    }
}
