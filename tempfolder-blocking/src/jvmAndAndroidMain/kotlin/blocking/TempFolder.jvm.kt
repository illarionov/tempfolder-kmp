/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

internal actual fun createPlatformTempFolder(namePrefix: String): TempFolder {
    return JvmTempFolder.create(namePrefix)
}

public class JvmTempFolder private constructor(
    private val jvmPath: Path,
) : TempFolder {
    override val path: String
        get() = jvmPath.toString()

    override fun resolve(name: String): String {
        return jvmPath.resolve(name).toString()
    }

    @OptIn(ExperimentalPathApi::class)
    override fun delete() {
        jvmPath.deleteRecursively()
    }

    public companion object {
        public fun create(
            namePrefix: String,
        ): JvmTempFolder {
            val hasPosixFilePermissionSupport = FileSystems.getDefault().supportedFileAttributeViews().contains("posix")
            val attrs: Array<FileAttribute<*>> = if (hasPosixFilePermissionSupport) {
                arrayOf(
                    PosixFilePermissions.asFileAttribute(
                        setOf(
                            PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                        ),
                    ),
                )
            } else {
                emptyArray()
            }

            @Suppress("SpreadOperator")
            val folder = Files.createTempDirectory(namePrefix, *attrs)
            return JvmTempFolder(folder)
        }
    }
}
