/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("OPT_IN_USAGE")

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("at.released.tempfolder.gradle.lint.binary-compatibility-validator")
    id("at.released.tempfolder.gradle.lint.android-lint")
    id("at.released.tempfolder.gradle.multiplatform.atomicfu")
    id("at.released.tempfolder.gradle.multiplatform.android")
    id("at.released.tempfolder.gradle.multiplatform.kotlin")
    id("at.released.tempfolder.gradle.multiplatform.publish")
    id("at.released.tempfolder.gradle.multiplatform.test.jvm")
}

group = "at.released.tempfolder"
version = tempfolderVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "tempfolder_blocking_version",
    envVariableName = "TEMPFOLDER_BLOCKING_VERSION",
).get()

android {
    namespace = "at.released.tempfolder.blocking"
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = false
            isIncludeAndroidResources = true
        }
    }
    packaging {
        resources.excludes += listOf(
            "META-INF/LICENSE.md",
            "META-INF/LICENSE-notice.md",
        )
    }
}

@OptIn(ExperimentalWasmDsl::class)
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
    linuxArm64 {
        setupLinuxInterops()
    }
    linuxX64 {
        setupLinuxInterops()
    }
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

    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withAndroidTarget()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.bytestring)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.assertk)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.rules)
        }
    }
}

private fun KotlinNativeTarget.setupLinuxInterops() = compilations.named("main") {
    cinterops {
        create("linux") {
            packageName("at.released.tempfolder.platform.linux")
        }
    }
}
