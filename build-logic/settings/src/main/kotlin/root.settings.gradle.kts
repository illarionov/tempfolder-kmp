/*
 * Copyright 2024-2025, Alexey Illarionov and the at-released-tempfolder project contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
