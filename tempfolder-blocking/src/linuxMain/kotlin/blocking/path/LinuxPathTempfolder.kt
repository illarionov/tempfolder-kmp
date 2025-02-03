/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking.path

import at.released.tempfolder.DeleteRecursivelyException
import at.released.tempfolder.TempfolderClosedException
import at.released.tempfolder.TempfolderClosedException.Companion.TEMPFOLDER_CLOSED_MESSAGE
import at.released.tempfolder.blocking.Tempfolder
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.asPathString
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import platform.posix.FTW_DEPTH
import platform.posix.FTW_PHYS
import platform.posix.errno
import platform.posix.getenv
import platform.posix.mkdtemp
import platform.posix.nftw
import platform.posix.remove

public class LinuxPathTempfolder private constructor(
    absolutePath: String,
) : Tempfolder<String> {
    override val root: String = absolutePath
    private val isClosed = atomic(false)
    override var deleteOnClose: Boolean by atomic(true)

    override fun getAbsolutePath(): TempfolderPathString = root.asPathString()

    override fun delete() {
        throwIfClosed()
        val code = nftw(
            root,
            @Suppress("UNUSED_ANONYMOUS_PARAMETER")
            staticCFunction { path, stat, typeFlag, ftw ->
                memScoped {
                    remove(path!!.toKString())
                }
            },
            64,
            FTW_DEPTH.or(FTW_PHYS),
        )
        if (code != 0) {
            throw DeleteRecursivelyException("Can not remove $root. Error $errno")
        }
    }

    override fun resolve(name: String): TempfolderPathString = "$root/$name".asPathString()

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        if (deleteOnClose) {
            delete()
        }
    }

    private fun throwIfClosed() {
        if (isClosed.value) {
            throw TempfolderClosedException(TEMPFOLDER_CLOSED_MESSAGE)
        }
    }

    public companion object {
        public fun create(
            namePrefix: String,
        ): LinuxPathTempfolder {
            val tmpdir = getenv("TMPDIR")?.toKString() ?: "/tmp"
            val template = "$tmpdir/${namePrefix}XXXXXX"
            val path = memScoped {
                val nativeTemplate = template.cstr.getPointer(this)
                val newPath = mkdtemp(nativeTemplate) ?: error("Can not create temp dir: error $errno")
                newPath.toKString()
            }
            return LinuxPathTempfolder(path)
        }
    }
}
