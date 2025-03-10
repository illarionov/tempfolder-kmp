/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

/**
 * An exception class for tempfolder-related errors.
 */
public open class TempDirectoryException : TempDirectoryPlatformIOException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
}

/**
 * Marker class for the platform IOException
 */
public expect open class TempDirectoryPlatformIOException : Exception {
    public constructor()
    public constructor(message: String?)
    public constructor(cause: Throwable)
    public constructor(message: String, cause: Throwable?)
}
