/*
 * Copyright 2024-2025, Alexey Illarionov and the at-released-tempfolder project contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
