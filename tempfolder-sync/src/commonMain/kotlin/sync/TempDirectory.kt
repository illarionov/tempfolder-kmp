/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:JvmName("TempfolderBuilder")

package at.released.tempfolder.sync

import at.released.tempfolder.TempDirectoryException
import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.dsl.CommonTempDirectoryConfig
import at.released.tempfolder.path.TempDirectoryPath
import kotlin.jvm.JvmName

internal expect fun createPlatformTempDirectory(config: CommonTempDirectoryConfig): TempDirectory<*>

/**
 * Creates a temporary directory on the file system.
 *
 * The absolute path of the created directory can be obtained by calling
 * [getAbsolutePath().asString()][TempDirectory.absolutePath].
 *
 * The returned [TempDirectory] object implements [AutoCloseable]. Сall [close()][TempDirectory.close] to close
 * its file descriptor and delete the temporary directory when it is no longer needed.
 * Set [TempDirectory.deleteOnClose] to false to preserve the directory, but [close()] must still be called.
 *
 * The configuration block [config] can be used to customize the base path, name prefix, and other options.
 *
 * @throws TempDirectoryIOException on errors during directory creation.
 */
@Throws(TempDirectoryIOException::class)
public fun createTempDirectory(
    block: CommonTempDirectoryConfig.() -> Unit = {},
): TempDirectory<*> {
    val config = CommonTempDirectoryConfig().apply(block)
    return createPlatformTempDirectory(config)
}

/**
 * Interface representing a temporary directory.
 *
 * This object provides the path to the created directory along with an open file descriptor or file handle.
 * It automatically deletes the directory when [close] is called.
 *
 * Use [createTempDirectory] to create temporary directory from common code.
 * Additional factories for platform-specific implementations are available on target platforms, offering more
 * flexible customization and usage capabilities.
 *
 * The [absolutePath] method returns the absolute path of the temporary directory.
 *
 * This object implements [AutoCloseable], call [close] when the directory is no longer needed.
 * The directory is automatically recursively deleted when [close] is called.
 * Set [TempDirectory.deleteOnClose] to false to preserve the directory; however, [close] must still be called.
 *
 * The open file descriptor or directory handle is available through the [TempDirectory.root] property.
 * It can be used with platform-specific filesystem functions (for example, `openat2()` on Linux).
 */
public interface TempDirectory<out FH : Any> : AutoCloseable {
    /**
     * Specifies whether the temporary directory should be deleted when [close] is called.
     */
    public var deleteOnClose: Boolean

    /**
     * Depending on the implementation, the absolute path to the created directory, the native open file
     * descriptor, or a native file handle.
     * Do not close the provided file descriptor yourself, as it will lead to incorrect behavior.
     *
     * On POSIX-compatible filesystems, consider using the `dup()` system call to duplicate the descriptor
     * to use with functions like `fopendir()`.
     *
     * The descriptor is closed when [close] is called.
     */
    public val root: FH

    /**
     * Returns the absolute path of the temporary directory.
     *
     * @throws TempDirectoryIOException if an error occurs while resolving the path.
     */
    @Throws(TempDirectoryIOException::class)
    public fun absolutePath(): TempDirectoryPath

    /**
     * Recursively deletes the temporary directory.
     *
     * This method is generally not used directly as the directory is deleted when [close] is invoked.
     *
     * Note that calling this method does not close the open file descriptor [root].
     *
     * @throws TempDirectoryIOException if an error occurs during deletion.
     */
    @Throws(TempDirectoryIOException::class)
    public fun delete()

    /**
     * Appends [name] to the [absolutePath] using the platform-specific file system separator and returns
     * the complete path for [name] within the temporary directory.
     */
    @Throws(TempDirectoryException::class)
    public fun append(name: String): TempDirectoryPath

    /**
     * Closes the temporary directory.
     *
     * If [deleteOnClose] is true, the directory is recursively deleted
     *
     * @throws TempDirectoryIOException if an error occurs during deletion or closing file descriptor.
     */
    override fun close()

    public companion object
}
