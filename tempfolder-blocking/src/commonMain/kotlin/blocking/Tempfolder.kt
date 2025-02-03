/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.dsl.CommonTempfolderConfig
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.path.TempfolderPathString

internal expect fun createPlatformTempFolder(config: CommonTempfolderConfig): Tempfolder<*>

@Throws(TempfolderIOException::class)
public fun Tempfolder(
    block: CommonTempfolderConfig.() -> Unit,
): Tempfolder<*> {
    val config = CommonTempfolderConfig().apply(block)
    return createPlatformTempFolder(config)
}

public interface Tempfolder<out FH : Any> : AutoCloseable {
    public var deleteOnClose: Boolean
    public val root: FH

    @Throws(TempfolderIOException::class)
    public fun getAbsolutePath(): TempfolderPathString

    @Throws(TempfolderIOException::class)
    public fun delete()

    @Throws(TempfolderIOException::class, TempfolderInvalidPathException::class)
    public fun resolve(name: String): TempfolderPathString

    override fun close()

    public companion object
}
