/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.path.PosixPathStringComponent.Companion.asPathComponent
import at.released.tempfolder.path.TempfolderPathEmptyException.Companion.PATH_IS_EMPTY_MESSAGE
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.append
import kotlinx.io.bytestring.buildByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.bytestring.indexOf
import kotlinx.io.bytestring.indices
import kotlinx.io.bytestring.isEmpty

internal const val UNIX_PATH_SEPARATOR = '/'.code.toByte()
internal val PATH_CURRENT_DIRECTORY = ".".toPosixPathString().asPathComponent()
internal val PATH_PARENT_DIRECTORY = "..".toPosixPathString().asPathComponent()

internal fun ByteString.isUtfSpecialDirectory(): Boolean =
    this == PATH_CURRENT_DIRECTORY.bytes || this == PATH_PARENT_DIRECTORY.bytes

internal fun PosixPathString.isSpecialDirectory(): Boolean = isCurrentDirectory() || isParentDirectory()
internal fun PosixPathString.isCurrentDirectory(): Boolean = bytes == PATH_CURRENT_DIRECTORY.bytes
internal fun PosixPathString.isParentDirectory(): Boolean = bytes == PATH_PARENT_DIRECTORY.bytes
internal fun PosixPathString.isAbsolute(): Boolean = bytes[0] == UNIX_PATH_SEPARATOR

@Throws(TempfolderInvalidPathException::class)
internal fun validateBasicPosixPath(path: ByteString) {
    if (path.isEmpty()) {
        throw TempfolderPathEmptyException(PATH_IS_EMPTY_MESSAGE)
    }
    if (path.indexOf(0) != -1) {
        throw (TempfolderInvalidCharacterException("Null character is not allowed in path"))
    }
}

internal fun validatePosixPathComponent(component: ByteString) {
    if (component.indexOf(UNIX_PATH_SEPARATOR) != -1) {
        throw (TempfolderInvalidCharacterException("A Unix path component must not contain a path separator"))
    }
}

@Throws(TempfolderInvalidPathException::class)
internal fun ByteString.appendPosixPath(
    path: String,
): ByteString {
    val source = this
    if (source.isEmpty()) {
        return path.encodeToByteString().also {
            validateBasicPosixPath(it)
        }
    }

    val appendPath = path.encodeToByteString()
    val firstNonSeparator = appendPath.indices.firstOrNull { appendPath[it] != UNIX_PATH_SEPARATOR } ?: return this

    return buildByteString(source.size + appendPath.size) {
        append(source)
        if (source[source.size - 1] != UNIX_PATH_SEPARATOR) {
            append(UNIX_PATH_SEPARATOR)
        }
        append(appendPath.substring(firstNonSeparator))
    }
}
