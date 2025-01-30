/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("at.released.tempfolder.gradle.multiplatform.distribution.aggregate")
}

dependencies {
    mavenSnapshotAggregation(projects.tempfolderBlocking)
}
