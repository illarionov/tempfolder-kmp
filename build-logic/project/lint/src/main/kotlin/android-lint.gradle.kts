/*
 * SPDX-FileCopyrightText: 2024-2026 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.lint

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * Convention plugin that configures Android Lint in projects with the Android Gradle plugin
 */
plugins.withId("com.android.kotlin.multiplatform.library") {
    apply(plugin = "com.android.lint")
    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
            lint.configureCommonAndroidLint()
        }
    }
}

project.plugins.withType(AppPlugin::class.java) {
    extensions.configure(CommonExtension::class.java) {
        lint.apply {
            configureCommonAndroidLint()
            checkDependencies = true
        }
    }
}

listOf(
    LibraryPlugin::class.java,
    DynamicFeaturePlugin::class.java,
    TestPlugin::class.java,
).forEach { agpLibraryPlugin ->
    plugins.withType(agpLibraryPlugin) {
        extensions.configure(CommonExtension::class.java) {
            lint.configureCommonAndroidLint()
        }
    }
}
