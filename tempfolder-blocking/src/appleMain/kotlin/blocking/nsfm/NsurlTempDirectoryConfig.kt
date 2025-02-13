/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.nsfm

import at.released.tempfolder.dsl.TempfolderDsl
import platform.Foundation.NSFileManager

@TempfolderDsl
public class NsurlTempDirectoryConfig internal constructor() {
    /**
     * The NSFileManager file manager used for file system operations.
     */
    public var fileManager: NSFileManager = NSFileManager.defaultManager

    /**
     * Base path for the temporary directory
     */
    public var base: NsurlTempBase = NsurlTempBase.Auto()

    public companion object {
        public fun auto(
            block: NsurlTempBase.Auto.() -> Unit = {},
        ): NsurlTempBase = NsurlTempBase.Auto().apply(block)
    }
}
