/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.path.TempfolderPathString
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString

internal expect fun createPlatformTempFolder(
    namePrefix: String = "tempfolderTest",
): Tempfolder<*>

public interface Tempfolder<out FH : Any> : AutoCloseable {
    public var deleteOnClose: Boolean

    public val root: FH

    @Throws(TempfolderIOException::class)
    public fun getAbsolutePath(): TempfolderPathString

    @Throws(TempfolderIOException::class)
    public fun delete()

    // TODO: remove?
    @Throws(TempfolderIOException::class)
    public fun resolve(name: String): TempfolderPathString

    override fun close()

    public companion object {
        @Throws(TempfolderIOException::class)
        public fun create(
            namePrefix: String = "wehTest",
        ): Tempfolder<*> = createPlatformTempFolder(namePrefix)
    }
}
