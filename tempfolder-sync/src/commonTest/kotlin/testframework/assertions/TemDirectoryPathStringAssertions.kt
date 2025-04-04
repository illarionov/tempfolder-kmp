/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.assertions

import assertk.Assert
import assertk.assertions.support.appendName
import at.released.tempfolder.path.PosixPath.Companion.toPosixPath
import at.released.tempfolder.path.TempDirectoryPath

public fun Assert<TempDirectoryPath>.isValidString(): Assert<String> = transform(
    appendName("toString", separator = "::"),
) { path ->
    path.asString()
}

public fun Assert<TempDirectoryPath>.dirname(): Assert<TempDirectoryPath> {
    return transform(appendName("dirname", separator = "::")) { path ->
        val noTrailingSlash: String = path.asString().trimEnd(Char::isAnyPathSeparator)
        val lastSlashIndex = noTrailingSlash.indices.reversed()
            .firstOrNull { noTrailingSlash[it].isAnyPathSeparator() } ?: noTrailingSlash.length
        val dirname = noTrailingSlash.substring(0, lastSlashIndex).ifEmpty { "." }
        dirname.toPosixPath()
    }
}

fun Assert<TempDirectoryPath>.basename(): Assert<TempDirectoryPath> {
    return transform(appendName("basename", separator = "::")) { path ->
        val basename = path.asString().trimEnd(Char::isAnyPathSeparator).substringAfterLastSlash().ifEmpty { "." }
        basename.toPosixPath()
    }
}

private fun String.substringAfterLastSlash(): String {
    val lastSlashIndex = indices.reversed().firstOrNull { this[it].isAnyPathSeparator() }
    return when (lastSlashIndex) {
        null -> this
        lastIndex -> ""
        else -> substring(lastSlashIndex + 1)
    }
}

private fun Char.isAnyPathSeparator() = this == '\\' || this == '/'
