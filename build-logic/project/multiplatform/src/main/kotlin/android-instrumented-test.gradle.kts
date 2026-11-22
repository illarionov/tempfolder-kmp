/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

package at.released.tempfolder.gradle.multiplatform

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * Convention plugin that configures Android instrumented tests
 */
plugins.withId("com.android.kotlin.multiplatform.library") {
    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
            withDeviceTest {
                multidex.enable = true
                instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                managedDevices.localDevices.register("pixel2api30") {
                    device = "Pixel 2"
                    sdkVersion = 30
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }
}
