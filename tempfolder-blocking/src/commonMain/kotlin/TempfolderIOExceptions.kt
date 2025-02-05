/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

/**
 * Exception thrown when a method cannot be called because the temporary directory has been closed
 */
public class TempfolderClosedException : TempfolderIOException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)

    public companion object {
        internal const val TEMPFOLDER_CLOSED_MESSAGE = "Temporary folder is closed"
    }
}

/**
 * Group of exceptions thrown when an error occurs during recursive deletion of the temporary directory
 */
public class DeleteRecursivelyException : TempfolderIOException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)

    public companion object {
        internal const val FAILED_TO_DELETE_MESSAGE =
            "Failed to delete one or more files. See suppressed exceptions for details"
    }
}

/**
 * An exception class for tempfolder-related I/O errors.
 */
public open class TempfolderIOException : TempfolderException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
}
