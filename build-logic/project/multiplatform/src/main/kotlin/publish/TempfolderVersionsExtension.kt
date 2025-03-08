/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.multiplatform.publish

import at.released.tempfolder.gradle.multiplatform.publish.PropertiesValueSource.Parameters
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.kotlin.dsl.create
import java.util.Properties
import javax.inject.Inject

private const val VERSION_PROPERTIES_PATH = "config/version.properties"

internal fun Project.createTempfolderVersionsExtension(): TempfolderVersionsExtension {
    val configFilePath: RegularFile = layout.settingsDirectory.file(VERSION_PROPERTIES_PATH)
    return extensions.create<TempfolderVersionsExtension>("tempfolderVersions", configFilePath)
}

open class TempfolderVersionsExtension @Inject constructor(
    private val providers: ProviderFactory,
    private val propertiesFile: RegularFile,
) {
    private val propertiesProvider: Provider<Map<String, String>> = providers.of(PropertiesValueSource::class.java) {
        parameters.configFilePath.set(propertiesFile)
    }.orElse(providers.provider { error("File $propertiesFile not found") })
    val rootVersion: Provider<String> = providers.gradleProperty("version")
        .orElse(providers.environmentVariable("TEMPFOLDER_VERSION"))
        .orElse(
            propertiesProvider.map { props ->
                props["tempfolder_version"]
                    ?: error("No `tempfolder_version` in $propertiesFile")
            },
        )

    fun getSubmoduleVersionProvider(
        propertiesFileKey: String,
        envVariableName: String,
        gradleKey: String = propertiesFileKey,
    ): Provider<String> = providers.gradleProperty(gradleKey)
        .orElse(providers.environmentVariable(envVariableName))
        .orElse(
            propertiesProvider.map { props ->
                props[propertiesFileKey] ?: rootVersion.get()
            },
        )
}

private abstract class PropertiesValueSource : ValueSource<Map<String, String>, Parameters> {
    override fun obtain(): Map<String, String> {
        val propsFile = parameters.configFilePath.get().asFile
        val props = Properties().apply {
            propsFile.bufferedReader().use { load(it) }
        }
        return props.map { it.key.toString() to it.value.toString() }.toMap()
    }

    interface Parameters : ValueSourceParameters {
        val configFilePath: RegularFileProperty
    }
}
