/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.fd

import at.released.tempfolder.dsl.CommonTempfolderConfig.Companion.DEFAULT_PREFIX
import at.released.tempfolder.dsl.TempfolderDsl

@TempfolderDsl
public class Wasip1TempDirectoryConfig internal constructor() {
    /**
     * Base path for the temporary directory
     */
    public var base: WasiP1TempBase = WasiP1TempBase.Auto

    /**
     * Prefix for the directory name.
     *
     * Default: "tempfolder-".
     */
    public var prefix: String = DEFAULT_PREFIX
}
