/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(UnsafeNumber::class, BetaInteropApi::class)

package at.released.tempfolder.sync.nsfm

import at.released.tempfolder.TempDirectoryIOException
import at.released.tempfolder.sync.nsfm.NsurlTempBase.SearchPathDomain
import at.released.tempfolder.sync.nsfm.NsurlTempBase.SearchPathDomain.LOCAL
import at.released.tempfolder.sync.nsfm.NsurlTempBase.SearchPathDomain.NETWORK
import at.released.tempfolder.sync.nsfm.NsurlTempBase.SearchPathDomain.SYSTEM
import at.released.tempfolder.sync.nsfm.NsurlTempBase.SearchPathDomain.USER
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSItemReplacementDirectory
import platform.Foundation.NSLocalDomainMask
import platform.Foundation.NSNetworkDomainMask
import platform.Foundation.NSSearchPathDomainMask
import platform.Foundation.NSSystemDomainMask
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(UnsafeNumber::class)
internal fun createAppleNsurlTempDirectory(
    fileManager: NSFileManager,
    base: NsurlTempBase,
): NSURL {
    check(base is NsurlTempBase.Auto)
    return memScoped {
        val error: ObjCObjectVar<NSError?> = alloc()
        val forDirectory = base.appropriateForUrl ?: getDefaultCacheDirectory(fileManager)
        fileManager.URLForDirectory(
            directory = NSItemReplacementDirectory,
            inDomain = if (base.appropriateForUrl != null) base.inDomain.mask else NSUserDomainMask,
            appropriateForURL = forDirectory,
            create = true,
            error = error.ptr,
        ) ?: throw TempDirectoryIOException(
            "Can not create temporarily directory: ${error.value?.localizedDescription}",
        )
    }
}

private fun NativePlacement.getDefaultCacheDirectory(
    fileManager: NSFileManager,
): NSURL {
    val error: ObjCObjectVar<NSError?> = alloc()
    return fileManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = error.ptr,
    ) ?: throw TempDirectoryIOException("Can not get cache directory: ${error.value?.localizedDescription}")
}

private val SearchPathDomain.mask: NSSearchPathDomainMask
    get() = when (this) {
        USER -> NSUserDomainMask
        LOCAL -> NSLocalDomainMask
        NETWORK -> NSNetworkDomainMask
        SYSTEM -> NSSystemDomainMask
    }
