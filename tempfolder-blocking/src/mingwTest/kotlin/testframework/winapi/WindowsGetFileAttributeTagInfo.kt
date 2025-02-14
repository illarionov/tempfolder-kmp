/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.testframework.winapi

import at.released.tempfolder.TempfolderWindowsIOException
import at.released.tempfolder.testframework.winapi.model.FileAttributes
import at.released.tempfolder.testframework.winapi.model.ReparseTag
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.FILE_ATTRIBUTE_TAG_INFO
import platform.windows.FILE_INFO_BY_HANDLE_CLASS
import platform.windows.GetFileInformationByHandleEx
import platform.windows.GetLastError
import platform.windows.HANDLE

internal fun windowsGetFileAttributeTagInfo(
    path: String,
    followBaseSymlink: Boolean = false,
): FileAttributeTagInfo = withOpenFileHandle(path, followBaseSymlink, HANDLE::getFileAttributeTagInfo)

internal fun HANDLE.getFileAttributeTagInfo(): FileAttributeTagInfo = memScoped {
    val fileAttributeTagInfo: FILE_ATTRIBUTE_TAG_INFO = alloc()
    val result = GetFileInformationByHandleEx(
        this@getFileAttributeTagInfo,
        FILE_INFO_BY_HANDLE_CLASS.FileAttributeTagInfo,
        fileAttributeTagInfo.ptr,
        sizeOf<FILE_ATTRIBUTE_TAG_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileAttributeTagInfo(
            FileAttributes(fileAttributeTagInfo.FileAttributes),
            ReparseTag(fileAttributeTagInfo.ReparseTag),
        )
    } else {
        throw TempfolderWindowsIOException("GetFileInformationByName(FILE_ATTRIBUTE_TAG_INFO) failed", GetLastError())
    }
}

internal data class FileAttributeTagInfo(
    val fileAttributes: FileAttributes,
    val reparseTag: ReparseTag,
) {
    val isSymlink = fileAttributes.isSymlinkOrReparsePoint && reparseTag.isSymlink
}
