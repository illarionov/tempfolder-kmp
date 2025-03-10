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
    id("at.released.tempfolder.gradle.multiplatform.js")
    id("at.released.tempfolder.gradle.multiplatform.publish")
    id("at.released.tempfolder.gradle.multiplatform.test.android-jvm")
    id("at.released.tempfolder.gradle.multiplatform.test.jvm")
    id("at.released.tempfolder.gradle.multiplatform.test.js")
    id("at.released.tempfolder.gradle.multiplatform.test.native")
    id("at.released.tempfolder.gradle.multiplatform.test.wasmWasi")
}

group = "at.released.tempfolder"
version = tempfolderVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "tempfolder_sync_version",
    envVariableName = "TEMPFOLDER_SYNC_VERSION",
).get()

android {
    namespace = "at.released.tempfolder.sync"
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
        compilerOptions {
            freeCompilerArgs.add("-Xwasm-attach-js-exception")
        }
        nodejs()
    }
    wasmWasi {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    optIn.add("kotlin.wasm.unsafe.UnsafeWasmMemoryApi")
                }
            }
        }
    }
    iosSimulatorArm64()
    iosArm64()
    iosX64()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()
    watchosDeviceArm64()
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
                group("androidNative") {
                    group("androidNativeX64") {
                        withAndroidNativeArm64()
                        withAndroidNativeX64()
                    }
                }
            }
            group("nodeJsCommon") {
                withWasmJs()
                withJs()
            }
        }
    }

    targets.withType<KotlinNativeTarget>().matching { it.konanTarget.family.isAppleFamily }.configureEach {
        // https://youtrack.jetbrains.com/issue/KT-73136
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
            implementation(projects.testIgnoreAnnotations)
            implementation(libs.assertk)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.rules)
        }
    }
}
