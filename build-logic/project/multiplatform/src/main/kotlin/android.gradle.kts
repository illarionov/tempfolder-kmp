/*
 * SPDX-FileCopyrightText: 2024-2026 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform

import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17

/*
 * Convention plugin that configures Android library target in projects with the Kotlin Multiplatform plugin
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("at.released.tempfolder.gradle.multiplatform.kotlin")
    id("at.released.tempfolder.gradle.lint.android-lint")
}

kotlin {
    android {
        compileSdk = versionCatalogs.named("libs").findVersion("androidCompileSdk").get().displayName.toInt()
        minSdk = versionCatalogs.named("libs").findVersion("androidMinSdk").get().displayName.toInt()
        aarMetadata.minCompileSdk = 1
        enableCoreLibraryDesugaring = false
        compilerOptions {
            jvmTarget.set(JVM_17)
        }
    }
}
