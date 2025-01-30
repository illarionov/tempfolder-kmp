/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

pluginManagement {
    includeBuild("build-logic/settings")
}

plugins {
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
        classpath("com.diffplug.spotless:spotless-plugin-gradle:7.0.2")
        classpath("com.saveourtool.diktat:diktat-gradle-plugin:2.0.0")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.30.0")
        classpath("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
        classpath("org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin:2.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
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
include("tempfolder-blocking")
include("doc:aggregate-documentation")
