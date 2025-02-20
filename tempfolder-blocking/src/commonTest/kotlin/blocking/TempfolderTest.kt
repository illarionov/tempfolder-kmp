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
import at.released.tempfolder.testframework.assertions.basename
import at.released.tempfolder.testframework.assertions.dirname
import at.released.tempfolder.testframework.assertions.isDirectory
import at.released.tempfolder.testframework.assertions.isNotExists
import at.released.tempfolder.testframework.assertions.isSamePathAs
import at.released.tempfolder.testframework.assertions.posixFileModeIfSupportedIsEqualTo
import at.released.tempfolder.testframework.isReadingDirectorySupported
import at.released.tempfolder.testframework.isSimulatorOrVirtualDevice
import at.released.tempfolder.testframework.platformFilesystem
import at.released.tempfolder.testframework.testTempDirRoot
import kotlinx.io.bytestring.ByteString
import kotlin.test.Test

class TempfolderTest {
    @Test
    fun tempfolder_test_success_case() {
        if (!isReadingDirectorySupported()) {
            return // Ignore test
        }

        val path: TempfolderPathString
        val tempDirectory = createTempfolder()
        path = tempDirectory.getAbsolutePath()
        try {
            assertThat(path, path.asStringOrDescription()).all {
                if (!isSimulatorOrVirtualDevice()) {
                    dirname().isSamePathAs(testTempDirRoot)
                }
                basename().transform { it.asString() }.startsWith(CommonTempfolderConfig.DEFAULT_PREFIX)
                isDirectory()
                posixFileModeIfSupportedIsEqualTo(USER_READ, USER_WRITE, USER_EXECUTE)
            }
            bootstrapSimpleSuccessTestTestHierarchy(tempDirectory)
        } catch (ex: Throwable) {
            tempDirectory.deleteOnClose = false
            throw ex
        } finally {
            tempDirectory.close()
        }

        assertThat(path, path.asStringOrDescription()).isNotExists()
    }

    public companion object {
        val TEST_FILE_CONTENT = ByteString(ByteArray(1024 * 1024, { (it % 0xff).toByte() }))

        fun bootstrapSimpleSuccessTestTestHierarchy(tempDirectory: Tempfolder<*>) {
            platformFilesystem.createFile(
                tempDirectory.append("file1.txt"),
                content = TEST_FILE_CONTENT,
            )
            platformFilesystem.createSymlink(
                "../../../../build",
                tempDirectory.append("build.link"),
            )

            tempDirectory.append("dir1").also { dir1Path ->
                platformFilesystem.createDirectory(dir1Path)
                platformFilesystem.createFile(
                    platformFilesystem.joinPath(dir1Path, "file2.txt"),
                )
            }
        }
    }
}
