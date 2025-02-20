/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.nsfm

import assertk.assertThat
import at.released.tempfolder.path.TempDirectoryPath
import at.released.tempfolder.path.asStringOrDescription
import at.released.tempfolder.sync.TempDirectory
import at.released.tempfolder.sync.TempDirectoryTest.Companion.bootstrapSimpleSuccessTestTestHierarchy
import at.released.tempfolder.testframework.assertions.isDirectory
import at.released.tempfolder.testframework.assertions.isNotExists
import kotlin.test.Test

class NsurlTempDirectoryTest {
    @Test
    fun nsurlTempfolder_test_success_case() {
        val path: TempDirectoryPath
        TempDirectory.createNsurlTempDirectory().use { tempDirectory ->
            path = tempDirectory.getAbsolutePath()
            assertThat(path).isDirectory()
            bootstrapSimpleSuccessTestTestHierarchy(tempDirectory)
        }
        assertThat(path, path.asStringOrDescription()).isNotExists()
    }
}
