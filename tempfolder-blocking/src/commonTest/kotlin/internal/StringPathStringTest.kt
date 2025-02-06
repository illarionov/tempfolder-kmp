/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.internal

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.tempfolder.path.TempfolderPathString.Companion.encoding
import at.released.tempfolder.path.TempfolderPathString.Encoding.UTF8
import at.released.tempfolder.path.toPosixPathString
import kotlinx.io.bytestring.decodeToString
import kotlin.test.Test

class StringPathStringTest {
    @Test
    fun asPathString_success_case() {
        val pathString = "test".toPosixPathString()
        assertThat(pathString.asString()).isEqualTo("test")
        assertThat(pathString.encoding).isEqualTo(UTF8)
        assertThat(pathString.bytes.decodeToString()).isEqualTo("test")
    }
}
