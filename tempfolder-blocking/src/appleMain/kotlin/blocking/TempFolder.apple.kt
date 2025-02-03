/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.dsl.CommonTempfolderConfig

internal actual fun createPlatformTempFolder(config: CommonTempfolderConfig): Tempfolder<*> =
    AppleNsurlTempfolder.create()
