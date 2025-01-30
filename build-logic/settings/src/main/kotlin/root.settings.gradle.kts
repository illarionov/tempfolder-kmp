/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.gradle.settings

/*
 * Base settings convention plugin for the use in library modules
 */
plugins {
    id("at.released.tempfolder.gradle.settings.common")
    id("at.released.tempfolder.gradle.settings.repositories")
}
