/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.tempfolder.path.TempDirectoryPath.Companion.encoding
import at.released.tempfolder.path.TempDirectoryPath.Encoding.UTF8
import kotlinx.io.bytestring.decodeToString
import kotlin.test.Test

class StringPathTest {
    @Test
    fun asPathString_success_case() {
        val pathString = "test".toPosixPath()
        assertThat(pathString.asString()).isEqualTo("test")
        assertThat(pathString.encoding).isEqualTo(UTF8)
        assertThat(pathString.bytes.decodeToString()).isEqualTo("test")
    }
}
