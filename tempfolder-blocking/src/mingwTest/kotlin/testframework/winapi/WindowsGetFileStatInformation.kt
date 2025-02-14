/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.winapi

import at.released.tempfolder.TempfolderWindowsIOException
import at.released.tempfolder.path.WindowsPathString
import at.released.tempfolder.platform.windows.FILE_STAT_INFORMATION
import at.released.tempfolder.platform.windows.GetFileInformationByName
import at.released.tempfolder.testframework.winapi.model.FileAttributes
import at.released.tempfolder.testframework.winapi.model.ReparseTag
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.wcstr
import platform.windows.GetLastError

private const val FILE_STAT_BY_NAME_INFO = 0

internal fun windowsGetFileStatInfo(
    path: String,
): FileStatInfo = memScoped {
    val statInfo: FILE_STAT_INFORMATION = alloc()
    val pathBytes = path.wcstr.placeTo(this)

    val result = GetFileInformationByName(
        pathBytes,
        FILE_STAT_BY_NAME_INFO,
        statInfo.ptr,
        sizeOf<FILE_STAT_INFORMATION>().toUInt(),
    )
    return if (result != 0) {
        FileStatInfo.create(statInfo)
    } else {
        throw TempfolderWindowsIOException("GetFileInformationByName(FILE_STAT_BY_NAME_INFO) failed", GetLastError())
    }
}

internal data class FileStatInfo(
    val fileId: Long,
    val creationTime: Long,
    val lastAccessTime: Long,
    val lastWriteTime: Long,
    val changeTime: Long,
    val allocationSize: Long,
    val endOfFile: Long,
    val fileAttributes: FileAttributes,
    val reparseTag: ReparseTag,
    val numberOfLinks: UInt,
    val effectiveAccess: UInt,
) {
    internal companion object {
        val FileStatInfo.isSymlink get() = fileAttributes.isSymlinkOrReparsePoint && reparseTag.isSymlink

        fun create(
            info: FILE_STAT_INFORMATION,
        ): FileStatInfo = FileStatInfo(
            fileId = info.FileId.QuadPart,
            creationTime = info.CreationTime.QuadPart,
            lastAccessTime = info.LastAccessTime.QuadPart,
            lastWriteTime = info.LastWriteTime.QuadPart,
            changeTime = info.ChangeTime.QuadPart,
            allocationSize = info.AllocationSize.QuadPart,
            endOfFile = info.EndOfFile.QuadPart,
            fileAttributes = FileAttributes(info.FileAttributes),
            reparseTag = ReparseTag(info.ReparseTag),
            numberOfLinks = info.NumberOfLinks,
            effectiveAccess = info.EffectiveAccess,
        )
    }
}
