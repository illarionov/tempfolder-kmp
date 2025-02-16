/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework

import assertk.all
import assertk.assertThat
import at.released.tempfolder.blocking.TempfolderTest.Companion.TEST_FILE_CONTENT
import at.released.tempfolder.blocking.createTempfolder
import at.released.tempfolder.dsl.TempfolderFileModeBit.GROUP_READ
import at.released.tempfolder.dsl.TempfolderFileModeBit.OTHER_READ
import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_READ
import at.released.tempfolder.dsl.TempfolderFileModeBit.USER_WRITE
import at.released.tempfolder.path.TempfolderPathString
import at.released.tempfolder.testframework.assertions.isDirectory
import at.released.tempfolder.testframework.assertions.isFile
import at.released.tempfolder.testframework.assertions.isSymlink
import at.released.tempfolder.testframework.assertions.posixFileModeIfSupportedIsEqualTo
import at.released.weh.test.ignore.annotations.IgnoreWasmWasi
import kotlin.test.Test

class PlatformFilesystemTestFunctionsTest {
    @IgnoreWasmWasi
    @Test
    fun platformTestFunctions_test_filesystem_functions() {
        createTempfolder {
            prefix = "platformFilesystemTestFunctionsTest-"
        }.use { tempDirectory ->
            val testFile1: TempfolderPathString = tempDirectory.resolve("file1.txt")
            platformFilesystem.createFile(testFile1, content = TEST_FILE_CONTENT)

            assertThat(testFile1).all {
                isFile()
                posixFileModeIfSupportedIsEqualTo(USER_READ, USER_WRITE, GROUP_READ, OTHER_READ)
            }

            val testDirectory1 = tempDirectory.resolve("dir1")
            platformFilesystem.createDirectory(testDirectory1)
            assertThat(testDirectory1).isDirectory()

            val testSymlink = tempDirectory.resolve("build.link")
            platformFilesystem.createSymlink("../../../../build", testSymlink)
            assertThat(testSymlink).isSymlink()
        }
    }
}
