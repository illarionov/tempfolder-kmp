/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("at.released.tempfolder.gradle.lint.android-lint")
    id("at.released.tempfolder.gradle.multiplatform.android")
    id("at.released.tempfolder.gradle.multiplatform.kotlin")
}

group = "at.released.weh"

android {
    namespace = "at.released.weh.ignore.annotations"
}

kotlin {
    androidTarget()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    jvm()
    js(IR) {
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }
    wasmWasi {
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
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()

    sourceSets {
        commonMain.dependencies {
            api(kotlin("test"))
        }
        jvmMain.dependencies {
            api(kotlin("test-junit"))
        }
        androidMain.dependencies {
            api(kotlin("test-junit"))
        }
    }
}
