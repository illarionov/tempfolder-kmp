/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

/**
 * An exception class for tempfolder-related non-I/O errors.
 */
public open class TempfolderRuntimeException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
}
