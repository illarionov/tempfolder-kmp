/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import at.released.tempfolder.gradle.settings.repository.googleFiltered

pluginManagement {
    includeBuild("../settings")
}

plugins {
    id("at.released.tempfolder.gradle.settings.root")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
    repositories {
        googleFiltered()
        mavenCentral()
        gradlePluginPortal()
    }
}

include("documentation")
include("lint")
include("multiplatform")

rootProject.name = "tempfolder-gradle-project-plugins"
