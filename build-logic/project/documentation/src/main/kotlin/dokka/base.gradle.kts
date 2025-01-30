/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.documentation.dokka

/*
 * Base configuration of Dokka
 */
plugins {
    id("org.jetbrains.dokka")
}

private val htmlResourcesRoot = rootProject.layout.projectDirectory.dir("doc/aggregate-documentation")

dokka {
    dokkaPublications.configureEach {
        suppressObviousFunctions.set(true)
        suppressInheritedMembers.set(true)
    }

    dokkaSourceSets.configureEach {
        includes.from(
            "MODULE.md",
        )
        sourceLink {
            localDirectory.set(project.layout.projectDirectory)
            val remoteUrlSubpath = project.path.replace(':', '/')
            remoteUrl("https://github.com/illarionov/tempfolder-kmp/tree/main$remoteUrlSubpath")
        }
    }

    pluginsConfiguration.html {
        homepageLink.set("https://tempfolder.released.at")
        footerMessage.set("Copyright 2024-2025, Alexey Illarionov and the at-released-tempfolder project contributors")
        customStyleSheets.from(
            htmlResourcesRoot.file("styles/font-jb-sans-auto.css"),
            htmlResourcesRoot.file("styles/tempfolder.css"),
        )
    }
}
