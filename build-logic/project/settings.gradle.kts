/*
 * Copyright 2024-2025, Alexey Illarionov and the at-released-tempfolder project contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
include("wasi-testsuite-codegen")
include("wasm-codegen")

rootProject.name = "tempfolder-gradle-project-plugins"
