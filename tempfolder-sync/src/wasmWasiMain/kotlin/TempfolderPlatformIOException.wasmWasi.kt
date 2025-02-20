/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package at.released.tempfolder

public actual open class TempfolderPlatformIOException : Exception {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
    public actual constructor(cause: Throwable) : super(cause)
    public actual constructor(message: String, cause: Throwable?) : super(message, cause)
}
