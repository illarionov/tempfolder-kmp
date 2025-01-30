/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform

import at.released.tempfolder.gradle.multiplatform.publish.createTempfolderVersionsExtension
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost

/*
 * Convention plugin with publishing defaults
 */
plugins {
    id("at.released.tempfolder.gradle.documentation.dokka.subproject")
    id("at.released.tempfolder.gradle.multiplatform.distribution.subproject")
    id("org.jetbrains.kotlin.multiplatform")
    id("com.vanniktech.maven.publish.base")
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

createTempfolderVersionsExtension()

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    publishing {
        repositories {
            maven {
                name = "PixnewsS3"
                setUrl("s3://maven.pixnews.ru/")
                credentials(AwsCredentials::class) {
                    accessKey = providers.environmentVariable("YANDEX_S3_ACCESS_KEY_ID").getOrElse("")
                    secretKey = providers.environmentVariable("YANDEX_S3_SECRET_ACCESS_KEY").getOrElse("")
                }
            }
        }
    }

    signAllPublications()

    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Empty(),
        ),
    )

    pom {
        name.set(project.name)
        description.set(
            "Utilities for creating temporary directories in Kotlin Multiplatform projects",
        )
        url.set("https://github.com/illarionov/tempfolder-kmp")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("illarionov")
                name.set("Alexey Illarionov")
                email.set("alexey@0xdc.ru")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/illarionov/tempfolder-kmp.git")
            developerConnection.set("scm:git:ssh://github.com:illarionov/tempfolder-kmp.git")
            url.set("https://github.com/illarionov/tempfolder-kmp")
        }
    }
}
