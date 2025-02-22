/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.assertions

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.support.appendName
import assertk.assertions.support.expected
import assertk.assertions.support.show
import at.released.tempfolder.dsl.TempDirectoryFileModeBit
import at.released.tempfolder.path.PosixPath.Companion.toPosixPath
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.testframework.platformFilesystem

fun Assert<TempDirectoryPath>.isSamePathAs(path: String) = given { path1 ->
    if (path1.asString() == path) {
        return
    }

    if (platformFilesystem.isSamePathAs(path1, path.toPosixPath())) {
        return
    }

    expected("path the same as:${show(path)} but was:${show(path1.asStringOrDescription())}")
}

fun Assert<TempDirectoryPath>.isDirectory(
    followBasenameSymlink: Boolean = false,
) = transform(appendName("isDirectory", separator = "::")) {
    platformFilesystem.isDirectory(it, followBasenameSymlink)
}.isTrue()

fun Assert<TempDirectoryPath>.isFile(
    followBasenameSymlink: Boolean = false,
) = transform(appendName("isFile", separator = "::")) {
    platformFilesystem.isFile(it, followBasenameSymlink)
}.isTrue()

fun Assert<TempDirectoryPath>.isSymlink() = transform { platformFilesystem.isSymlink(it) }.isTrue()

private fun Assert<TempDirectoryPath>.exists(
    followBasenameSymlink: Boolean = false,
): Assert<Boolean> = transform(appendName("exists", separator = "::")) { path ->
    platformFilesystem.isExists(path, followBasenameSymlink)
}

public fun Assert<TempDirectoryPath>.isExists(): Unit = exists().isTrue()

public fun Assert<TempDirectoryPath>.isNotExists(): Unit = exists().isFalse()

public fun Assert<TempDirectoryPath>.posixFileMode(
    withSuidGidSticky: Boolean = false,
    followBasenameSymlink: Boolean = false,
): Assert<Set<TempDirectoryFileModeBit>> = transform(appendName("fileMode", separator = ".")) { path ->
    val fileMode = platformFilesystem.getFileMode(path, followBasenameSymlink)
    if (withSuidGidSticky) {
        fileMode
    } else {
        fileMode - setOf(TempDirectoryFileModeBit.SUID, TempDirectoryFileModeBit.SGID, TempDirectoryFileModeBit.STICKY)
    }
}

public fun Assert<TempDirectoryPath>.posixFileModeIfSupportedIsEqualTo(
    vararg expectedBits: TempDirectoryFileModeBit,
    withSuidGidSticky: Boolean = false,
): Unit = given { path ->
    if (!platformFilesystem.isPosixFileModeSupported) {
        return
    }
    assertThat(path).posixFileMode(withSuidGidSticky).isEqualTo(expectedBits.toSet())
}
