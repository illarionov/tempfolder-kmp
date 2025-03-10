/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.lint

import io.gitlab.arturbosch.detekt.Detekt

/*
 * Convention plugin that creates and configures task designated to run Detekt static code analyzer
 */
plugins {
    id("io.gitlab.arturbosch.detekt")
}

val detektCheck = tasks.register("detektCheck", Detekt::class) {
    description = "Custom detekt for to check all modules"

    this.config.setFrom(configRootDir.file("detekt.yml"))
    setSource(
        lintedFileTree
            .matching {
                exclude("**/resources/**")
            }
            .filter {
                it.name.endsWith(".kt") || it.name.endsWith(".kts")
            },
    )
    basePath = layout.settingsDirectory.toString()

    parallel = true
    ignoreFailures = false
    buildUponDefaultConfig = true
    allRules = true

    reports {
        html.required.set(true)
        md.required.set(true)
        txt.required.set(false)
        sarif.required.set(true)

        xml.outputLocation.set(file("build/reports/detekt/report.xml"))
        html.outputLocation.set(file("build/reports/detekt/report.html"))
        txt.outputLocation.set(file("build/reports/detekt/report.txt"))
        sarif.outputLocation.set(file("build/reports/detekt/report.sarif"))
    }
}

dependencies {
    detektPlugins(versionCatalogs.named("libs").findLibrary("detekt.formatting").get())
}
