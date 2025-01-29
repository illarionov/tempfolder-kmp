/*
 * Copyright 2024-2025, Alexey Illarionov and the at-released-tempfolder project contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

internal expect fun createPlatformTempFolder(
    namePrefix: String = "tempfolderTest",
): TempFolder

public interface TempFolder {
    public val path: String
    public fun delete()

    public fun resolve(name: String): String

    public companion object {
        public fun create(
            namePrefix: String = "wehTest",
        ): TempFolder = createPlatformTempFolder(namePrefix)
    }
}
