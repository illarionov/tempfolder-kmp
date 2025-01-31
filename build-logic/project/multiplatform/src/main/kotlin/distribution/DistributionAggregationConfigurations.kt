/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.distribution

import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.named

@Suppress("UnstableApiUsage")
internal class DistributionAggregationConfigurations(
    objects: ObjectFactory,
    configurations: ConfigurationContainer,
) {
    val mavenSnapshotAggregation = configurations.dependencyScope("mavenSnapshotAggregation")
    val mavenSnapshotAggregationFiles = configurations.resolvable("mavenSnapshotAggregationFiles") {
        extendsFrom(mavenSnapshotAggregation.get())
        attributes {
            setupMavenSnapshotAggregationAttributes(objects)
        }
    }

    companion object {
        fun AttributeContainer.setupMavenSnapshotAggregationAttributes(objects: ObjectFactory) {
            attribute(USAGE_ATTRIBUTE, objects.named("tempfolder-runtime"))
            attribute(CATEGORY_ATTRIBUTE, objects.named("local-maven-snapshot"))
        }
    }
}
