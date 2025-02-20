/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage", "GENERIC_VARIABLE_WRONG_DECLARATION")

package at.released.tempfolder.gradle.multiplatform.distribution

import at.released.tempfolder.gradle.multiplatform.distribution.DistributionAggregationConfigurations.Companion.setupMavenSnapshotAggregationAttributes
import com.vanniktech.maven.publish.MavenPublishBasePlugin

/*
 * Plugin that configures the publication to the local directory and its sharing via consumable configuration
 */
private val rootVersion = provider { project.version }
private val localMavenRootDirectory: Provider<Directory> = layout.buildDirectory.dir("localMaven").zip(
    rootVersion.map { "maven-at-released-tempfolder-$it" },
    Directory::dir,
)

plugins.withType<MavenPublishBasePlugin> {
    addLocalMavenSnapshotRepository()
    val publishAllPublicationsTask = setupPublishTasks()
    sharePublicationInConsumableConfiguration(publishAllPublicationsTask)
}

private fun addLocalMavenSnapshotRepository() {
    extensions.getByType<PublishingExtension>().repositories.maven {
        name = "LocalMavenSnapshot"
        setUrl(localMavenRootDirectory)
    }
}

private fun setupPublishTasks(): TaskProvider<*> {
    val cleanupDownloadableReleaseRootTask = tasks.register<CleanupDirectoryTask>("cleanupDownloadableRelease") {
        inputDirectory.set(localMavenRootDirectory)
    }
    tasks.withType<PublishToMavenRepository>().configureEach {
        dependsOn(cleanupDownloadableReleaseRootTask)
    }

    val publishAllPublicationsTask = tasks.named("publishAllPublicationsToLocalMavenSnapshotRepository")
    publishAllPublicationsTask.configure {
        dependsOn(cleanupDownloadableReleaseRootTask)
    }
    return publishAllPublicationsTask
}

private fun sharePublicationInConsumableConfiguration(
    publishTask: TaskProvider<*>,
) {
    val mavenSnapshotReleaseElements = configurations.consumable("mavenSnapshotReleaseElements") {
        attributes {
            setupMavenSnapshotAggregationAttributes(objects)
        }
    }

    mavenSnapshotReleaseElements.configure {
        outgoing {
            artifact(localMavenRootDirectory) {
                builtBy(publishTask)
            }
        }
    }
}
