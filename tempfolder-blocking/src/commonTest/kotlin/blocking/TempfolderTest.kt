/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import assertk.all
import assertk.assertThat
import assertk.assertions.startsWith
import at.released.tempfolder.dsl.CommonTempfolderConfig
import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_EXECUTE
import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_READ
import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_WRITE
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.testTempDirRoot
import at.released.tempfolder.testframework.assertions.basename
import at.released.tempfolder.testframework.assertions.dirname
import at.released.tempfolder.testframework.assertions.isDirectory
import at.released.tempfolder.testframework.assertions.isNotExists
import at.released.tempfolder.testframework.assertions.isSamePathAs
import at.released.tempfolder.testframework.assertions.posixFileModeIfSupportedIsEqualTo
import at.released.tempfolder.testframework.platformFilesystem
import at.released.weh.test.ignore.annotations.IgnoreAndroid
import at.released.weh.test.ignore.annotations.IgnoreApple
import at.released.weh.test.ignore.annotations.IgnoreJs
import at.released.weh.test.ignore.annotations.IgnoreJvm
import at.released.weh.test.ignore.annotations.IgnoreMingw
import at.released.weh.test.ignore.annotations.IgnoreWasmJs
import at.released.weh.test.ignore.annotations.IgnoreWasmWasi
import kotlinx.io.bytestring.ByteString
import kotlin.test.Test

class TempfolderTest {
    @IgnoreApple
    @IgnoreJs
    @IgnoreMingw
    @IgnoreWasmJs
    @IgnoreWasmWasi
    @Test
    fun tempfolder_test_success_case() {
        val path: TempfolderPathString
        createTempfolder().use { tempDirectory ->
            path = tempDirectory.getAbsolutePath()
            assertThat(path).all {
                dirname().isSamePathAs(testTempDirRoot)
                basename().transform { it.asString() }.startsWith(CommonTempfolderConfig.DEFAULT_PREFIX)
                isDirectory()
                posixFileModeIfSupportedIsEqualTo(USER_READ, USER_WRITE, USER_EXECUTE)
            }
            platformFilesystem.createFile(
                tempDirectory.resolve("file1.txt"),
                content = TEST_FILE_CONTENT,
            )
            platformFilesystem.createSymlink(
                "../../../../build",
                tempDirectory.resolve("build.link"),
            )

            tempDirectory.resolve("dir1").also { dir1Path ->
                platformFilesystem.createDirectory(dir1Path)
                platformFilesystem.createFile(
                    platformFilesystem.resolvePath(dir1Path, "file2.txt"),
                )
            }
        }
        assertThat(path, path.asStringOrDescription()).isNotExists()
    }

    companion object {
        val TEST_FILE_CONTENT = ByteString(ByteArray(1024 * 1024, { (it % 0xff).toByte() }))
    }
}
