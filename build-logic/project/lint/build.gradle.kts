/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    `kotlin-dsl`
}

group = "at.released.tempfolder.gradle.lint"

dependencies {
    implementation(libs.detekt.plugin)
    implementation(libs.agp.plugin.api)
    implementation(libs.diktat.plugin)
    implementation(libs.kotlinx.binary.compatibility.validator.plugin)
    implementation(libs.spotless.plugin)
}
