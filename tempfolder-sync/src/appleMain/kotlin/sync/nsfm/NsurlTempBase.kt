/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.nsfm

import at.released.tempfolder.dsl.TempfolderDsl
import platform.Foundation.NSURL

@TempfolderDsl
public sealed class NsurlTempBase {
    public enum class SearchPathDomain {
        USER,
        LOCAL,
        NETWORK,
        SYSTEM,
    }

    /**
     * The base directory is determined automatically.
     *
     * [appropriateForUrl] / [inDomain]: The domain and file URL, used to specify the the preferred volume for
     * creating the temporary directory.
     * Defaults to NSCachesDirectory in NSUserDomainMask.
     *
     * See [URLForDirectory:inDomain:appropriateForURL:create:error:](https://developer.apple.com/documentation/foundation/nsfilemanager/1407693-urlfordirectory)
     */
    @TempfolderDsl
    public class Auto internal constructor(
        public var appropriateForUrl: NSURL? = null,
        public var inDomain: SearchPathDomain = SearchPathDomain.USER,
    ) : NsurlTempBase()
}
