/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("at.released.tempfolder.gradle.lint.detekt")
    id("at.released.tempfolder.gradle.lint.diktat")
    id("at.released.tempfolder.gradle.lint.spotless")
}

tasks.register("styleCheck") {
    group = "Verification"
    description = "Runs code style checking tools (excluding tests)"
    dependsOn("detektCheck", "spotlessCheck", "diktatCheck")
}
