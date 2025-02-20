/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.sync.delete

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.tempfolder.TempDirectoryDescriptor
import at.released.tempfolder.path.WasiPath.Companion.asWasiPathComponent
import at.released.tempfolder.path.WasiPath.Companion.toWasiPathString
import at.released.tempfolder.sync.delete.DirStream.DirStreamItem
import at.released.tempfolder.wasip1.ReadDirPage
import kotlinx.io.bytestring.encodeToByteString
import kotlin.test.Test

class OpenDirStreamTest {
    @Test
    fun openDirStream_readNext_should_return_end_of_stream_on_empty_list() {
        val readDirOrThrow = { _: TempDirectoryDescriptor, _: Int ->
            ReadDirPage(DEFAULT_ENTRIES, isFull = true)
        }

        val openDirStream = OpenDirStream(
            dirfd = TempDirectoryDescriptor(3),
            basename = "test".toWasiPathString().asWasiPathComponent(),
            readDirOrThrow = readDirOrThrow,
            closeOrThrow = { },
        )
        val entry = openDirStream.readNext()

        assertThat(entry).isEqualTo(DirStreamItem.EndOfStream)
    }

    companion object {
        internal val DEFAULT_ENTRIES = listOf(
            DirStreamItem.Entry(".".encodeToByteString(), true),
            DirStreamItem.Entry("..".encodeToByteString(), true),
        )
    }
}
