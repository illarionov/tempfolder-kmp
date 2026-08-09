/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

pluginManagement {
    includeBuild("build-logic/settings")
    includeBuild("build-logic/project")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("at.released.tempfolder.gradle.settings.root")
}

// Workaround for https://github.com/gradle/gradle/issues/26020
buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.32.0")
        classpath("org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin:2.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
        classpath(
            "org.jetbrains.kotlinx.binary-compatibility-validator:" +
                    "org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin:0.17.0",
        )
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.27.0")
    }
}

rootProject.name = "tempfolder-kmp"

// include("aggregate-documentation")
include("aggregate-distribution")
include("tempfolder-sync")
include("test-ignore-annotations")
include("doc:aggregate-documentation")
