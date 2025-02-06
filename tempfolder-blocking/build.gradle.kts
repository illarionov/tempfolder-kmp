/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("OPT_IN_USAGE")

import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

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

    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withAndroidTarget()
            }
            group("native") {
                group("posix200809") {
                    group("androidNative")
                    group("apple")
                    group("linux")
                }
            }
        }
    }

    targets.withType<KotlinNativeTarget>().matching { it.konanTarget.family.isAppleFamily }.configureEach {
        if (Os.isFamily("mac")) {
            compilations.named("main") {
                cinterops.create("apple") {
                    packageName("at.released.tempfolder.platform.apple")
                }
            }
        }
    }
    targets.withType<KotlinNativeTarget>().matching { it.konanTarget.family == Family.LINUX }.configureEach {
        compilations.named("main") {
            cinterops.create("linux") {
                packageName("at.released.tempfolder.platform.linux")
            }
        }
    }
    targets.withType<KotlinNativeTarget>().matching { it.konanTarget.family == Family.ANDROID }.configureEach {
        compilations.named("main") {
            cinterops {
                create("androidnative") {
                    packageName("at.released.tempfolder.platform.androidnative")
                }
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
