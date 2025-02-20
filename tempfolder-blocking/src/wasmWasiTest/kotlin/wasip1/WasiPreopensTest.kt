/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MagicNumber")

package at.released.tempfolder.wasip1

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.path.WasiPathString.Companion.toWasiPathString
import at.released.tempfolder.wasip1.WasiPreopens.Companion.COMPARATOR_LONGEST_THEN_FD_COMPARATOR
import kotlin.test.Test

class WasiPreopensTest {
    @Test
    fun root_from_path_comparator_should_retur_preopens_in_correct_order() {
        val preopenPairs = listOf(
            5 to "/tmp/dir/3",
            4 to "/tmp/dir/2",
            3 to "/tmp/dir/1",
            6 to "/tmp/dir/2/match",
        ).map { TempDirectoryDescriptor(it.first) to it.second.toWasiPathString() }

        assertThat(
            preopenPairs.sortedWith(COMPARATOR_LONGEST_THEN_FD_COMPARATOR).map { it.first.fd to it.second.asString() },
        ).isEqualTo(
            listOf(
                6 to "/tmp/dir/2/match",
                3 to "/tmp/dir/1",
                4 to "/tmp/dir/2",
                5 to "/tmp/dir/3",
            ),
        )
    }
}
