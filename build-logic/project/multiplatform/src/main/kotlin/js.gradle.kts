/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.binaryen.BinaryenRootExtension
import org.jetbrains.kotlin.gradle.targets.js.binaryen.BinaryenRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin

/*
 * Convention plugin that configures JS and Wasm Wasi target in Kotlin Multiplatform
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType<KotlinJsIrTarget>().configureEach {
            compilerOptions {
                target = "es2015"
                sourceMap = true
            }
        }
    }
}

plugins.withType<NodeJsPlugin> {
    extensions.configure<NodeJsEnvSpec> {
        version = versionCatalogs.named("libs").findVersion("nodejs").get().toString()
    }
}

plugins.withType<BinaryenRootPlugin> {
    extensions.configure<BinaryenRootExtension> {
        version = versionCatalogs.named("libs").findVersion("binaryen").get().toString()
    }
}
