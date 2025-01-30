/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask

/*
 * Module responsible for aggregating Dokka documentation from subprojects
 */
plugins {
    id("at.released.tempfolder.gradle.documentation.dokka.base")
}

group = "at.released.tempfolder"

private val websiteOutputDirectory = layout.buildDirectory.dir("outputs/website")

dokka {
    dokkaPublications.configureEach {
        moduleName.set("Tempfolder KMP")
        includes.from("FRONTPAGE.md")
    }
}

val dokkaHtmlOutput = tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml")
    .flatMap(DokkaGeneratePublicationTask::outputDirectory)

tasks.register<Sync>("buildWebsite") {
    description = "Assembles the final website from Dokka output"
    from(dokkaHtmlOutput)
    from(layout.projectDirectory.dir("root"))
    into(websiteOutputDirectory)
}

dependencies {
    dokka(projects.tempfolderBlocking)
}
