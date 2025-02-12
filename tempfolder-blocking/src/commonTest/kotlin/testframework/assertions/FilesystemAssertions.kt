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
import at.released.tempfolder.dsl.TempfolderFileModeBit
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.toPosixPathString
import at.released.tempfolder.testframework.platformFilesystem

fun Assert<TempfolderPathString>.isSamePathAs(path: String) = given { path1 ->
    if (path1.asString() == path) {
        return
    }

    if (platformFilesystem.isSamePathAs(path1, path.toPosixPathString())) {
        return
    }

    expected("path the same as:${show(path)}")
}

fun Assert<TempfolderPathString>.isDirectory(
    followBasenameSymlink: Boolean = false,
) = transform { platformFilesystem.isDirectory(it, followBasenameSymlink) }.isTrue()

fun Assert<TempfolderPathString>.isFile(
    followBasenameSymlink: Boolean = false,
) = transform { platformFilesystem.isFile(it, followBasenameSymlink) }.isTrue()

fun Assert<TempfolderPathString>.isSymlink() = transform { platformFilesystem.isSymlink(it) }.isTrue()

private fun Assert<TempfolderPathString>.exists(
    followBasenameSymlink: Boolean = false,
): Assert<Boolean> = transform(appendName("exists", separator = "::")) { path ->
    platformFilesystem.isExists(path, followBasenameSymlink)
}

public fun Assert<TempfolderPathString>.isExists(): Unit = exists().isTrue()

public fun Assert<TempfolderPathString>.isNotExists(): Unit = exists().isFalse()

public fun Assert<TempfolderPathString>.posixFileMode(
    withSuidGidSticky: Boolean = false,
    followBasenameSymlink: Boolean = false,
): Assert<Set<TempfolderFileModeBit>> = transform(appendName("fileMode", separator = ".")) { path ->
    val fileMode = platformFilesystem.getFileMode(path, followBasenameSymlink)
    if (withSuidGidSticky) {
        fileMode
    } else {
        fileMode - setOf(TempfolderFileModeBit.SUID, TempfolderFileModeBit.SGID, TempfolderFileModeBit.STICKY)
    }
}

public fun Assert<TempfolderPathString>.posixFileModeIfSupportedIsEqualTo(
    vararg expectedBits: TempfolderFileModeBit,
    withSuidGidSticky: Boolean = false,
): Unit = given { path ->
    if (!platformFilesystem.isPosixFileModeSupported) {
        return
    }
    assertThat(path).posixFileMode(withSuidGidSticky).isEqualTo(expectedBits.toSet())
}
