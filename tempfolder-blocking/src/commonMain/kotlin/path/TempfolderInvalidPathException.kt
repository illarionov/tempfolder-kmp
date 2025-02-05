/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.TempfolderException

/**
 * Exception thrown when encoding or decoding a path to a UTF string fails.
 */
public class TempfolderCharacterCodingException : TempfolderInvalidPathException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
}

/**
 * Exception thrown when the path includes a character that is not permitted by the file system.
 * The path may still be a valid UTF string.
 */
public class TempfolderInvalidCharacterException : TempfolderInvalidPathException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
}

/**
 * Exception thrown when the path string is empty.
 */
public class TempfolderPathEmptyException : TempfolderInvalidPathException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
    public companion object {
        internal const val PATH_IS_EMPTY_MESSAGE = "Path is empty"
    }
}

/**
 * Base exception for path processing errors.
 */
public open class TempfolderInvalidPathException : TempfolderException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
    public companion object
}
