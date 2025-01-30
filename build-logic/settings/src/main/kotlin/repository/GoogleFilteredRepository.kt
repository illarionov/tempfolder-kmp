/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.settings.repository

import org.gradle.api.artifacts.dsl.RepositoryHandler

public fun RepositoryHandler.googleFiltered() {
    google {
        mavenContent {
            // https://maven.google.com/web/index.html
            includeGroupByRegex("""android\.arch\..*""")
            includeGroupByRegex("""androidx\..+""")
            includeGroupByRegex("""com\.android(?:\..+)?""")
            includeGroupByRegex("""com\.crashlytics\.sdk\.android\..*""")
            includeGroupByRegex("""com\.google\.ads\..*""")
            includeGroupByRegex("""com\.google\.android\..*""")
            listOf(
                "com.google.ambient.crossdevice",
                "com.google.androidbrowserhelper",
                "com.google.ar",
                "com.google.ar.sceneform",
                "com.google.ar.sceneform.ux",
                "com.google.assistant.appactions",
                "com.google.assistant.suggestion",
                "com.google.camerax.effects",
                "com.google.chromeos",
                "com.google.d2c",
                "com.google.fhir",
                "com.google.firebase",
                "com.google.firebase.appdistribution",
                "com.google.firebase.crashlytics",
                "com.google.firebase.firebase-perf",
                "com.google.firebase.testlab",
                "com.google.gms",
                "com.google.gms.google-services",
                "com.google.jacquard",
                "com.google.mediapipe",
                "com.google.mlkit",
                "com.google.net.cronet",
                "com.google.oboe",
                "com.google.prefab",
                "com.google.relay",
                "com.google.test.platform",
                "com.google.testing.platform",
                "io.fabric.sdk.android",
                "tools.base.build-system.debug",
                "zipflinger",
            ).map(::includeGroup)

            includeModuleByRegex(
                """org\.jetbrains\.kotlin""",
                """kotlin-ksp|kotlin-symbol-processing-api""",
            )
        }
    }
}
