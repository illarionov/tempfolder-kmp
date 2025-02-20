/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:JvmName("TempfolderBuilder")

package at.released.tempfolder.sync

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.dsl.CommonTempDirectoryConfig
import at.released.tempfolder.path.TempDirectoryInvalidPathException
import at.released.tempfolder.path.TempDirectoryPath
import kotlin.jvm.JvmName

internal expect fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*>

@Throws(TempDirectoryIOException::class)
public fun createTempDirectory(
    block: CommonTempDirectoryConfig.() -> Unit = {},
): TempDirectory<*> {
    val config = CommonTempDirectoryConfig().apply(block)
    return createPlatformTempDirectory(config)
}

public interface TempDirectory<out FH : Any> : AutoCloseable {
    public var deleteOnClose: Boolean
    public val root: FH

    @Throws(TempDirectoryIOException::class)
    public fun getAbsolutePath(): TempDirectoryPath

    @Throws(TempDirectoryIOException::class)
    public fun delete()

    @Throws(TempDirectoryIOException::class, TempDirectoryInvalidPathException::class)
    public fun append(name: String): TempDirectoryPath

    override fun close()

    public companion object
}
