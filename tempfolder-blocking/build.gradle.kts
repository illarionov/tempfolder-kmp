/*
 * Copyright 2024-2025, Alexey Illarionov and the at-released-tempfolder project contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("at.released.tempfolder.gradle.lint.android-lint")
    id("at.released.tempfolder.gradle.multiplatform.kotlin")
    id("at.released.tempfolder.gradle.multiplatform.publish")
}

group = "at.released.tempfolder"
version = tempfolderVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "tempfolder_blocking_version",
    envVariableName = "TEMPFOLDER_BLOCKING_VERSION",
).get()

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvm()
    js(IR) {
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }
    iosSimulatorArm64()
    iosArm64()
    iosX64()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.bytestring)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
