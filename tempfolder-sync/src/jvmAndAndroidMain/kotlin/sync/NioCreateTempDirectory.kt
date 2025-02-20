/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync

import at.released.tempfolder.TempfolderException
import at.released.tempfolder.TempfolderIOException
import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.path.TempfolderInvalidPathException
import at.released.tempfolder.sync.NioTempBase.Auto
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.InvalidPathException
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createDirectory
import kotlin.io.path.isDirectory

@Throws(TempfolderException::class)
internal fun createNioTempDirectory(
    base: NioTempBase,
    mode: Set<TempfolderFileModeBit>,
    nameGenerator: () -> String,
): Path {
    val tempRoot = resolveBase(base)
    val tempDirectoryPath = (1..MAX_CREATE_DIRECTORY_ATTEMPTS).firstNotNullOfOrNull {
        val name = nameGenerator()
        tryCreateTempfolder(tempRoot, name, mode.toNioPosixPermissions())
    }
    return tempDirectoryPath ?: throw TempfolderIOException("Can not create temp folder: max attempts reached")
}

private fun resolveBase(base: NioTempBase): Path {
    return when (base) {
        is Auto -> getDefaultPath(base.fileSystem)
        is Path -> try {
            base.toRealPath()
        } catch (ioe: IOException) {
            throw TempfolderIOException("Failed to resolve temp dir root", ioe)
        }

        else -> error("Not expected")
    }
}

@Throws(TempfolderIOException::class)
private fun getDefaultPath(
    fileSystem: FileSystem,
): Path {
    val tmpDir = System.getProperty("java.io.tmpdir")
    return try {
        fileSystem.getPath(tmpDir).toRealPath()
    } catch (ie: InvalidPathException) {
        throw TempfolderInvalidPathException(ie)
    } catch (ioe: IOException) {
        throw TempfolderIOException("Failed to resolve temp dir root", ioe)
    }
}

@Suppress("ThrowsCount")
@Throws(TempfolderIOException::class)
private fun tryCreateTempfolder(
    base: Path,
    directoryName: String,
    mode: Set<PosixFilePermission>,
): Path? {
    val modeAttr = if (base.fileSystem.supportedFileAttributeViews().contains("posix")) {
        arrayOf(PosixFilePermissions.asFileAttribute(mode))
    } else {
        emptyArray()
    }

    val path = try {
        base.resolve(directoryName)
    } catch (ie: InvalidPathException) {
        throw TempfolderInvalidPathException(ie)
    }

    return try {
        path.createDirectory(attributes = modeAttr).toRealPath()
    } catch (@Suppress("SwallowedException") fe: FileAlreadyExistsException) {
        null
    } catch (ioe: IOException) {
        if (path.isDirectory(NOFOLLOW_LINKS)) {
            null
        } else {
            throw TempfolderIOException("Failed to create temp directory", ioe)
        }
    } catch (uoe: UnsupportedOperationException) {
        throw TempfolderIOException("DFailed to create temp directory", uoe)
    }
}
