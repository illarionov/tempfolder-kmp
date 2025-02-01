/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder

public class TempfolderClosedException : TempfolderIOException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)

    public companion object {
        internal const val TEMPFOLDER_CLOSED_MESSAGE = "Temporary folder is closed"
    }
}

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

public open class TempfolderIOException : TempfolderPlatformIOException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
}

public expect open class TempfolderPlatformIOException : Exception {
    public constructor()
    public constructor(message: String?)
    public constructor(cause: Throwable)
    public constructor(message: String, cause: Throwable?)
}
