/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.TempDirectoryException

/**
 * Exception thrown when encoding or decoding a path to a UTF string fails.
 */
public class TempDirectoryCharacterCodingException : TempDirectoryInvalidPathException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
}

/**
 * Exception thrown when the path includes a character that is not permitted by the file system.
 * The path may still be a valid UTF string.
 */
public class TempDirectoryInvalidCharacterException : TempDirectoryInvalidPathException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
}

/**
 * Exception thrown when the path string is empty.
 */
public class TempDirectoryPathEmptyException : TempDirectoryInvalidPathException {
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
public open class TempDirectoryInvalidPathException : TempDirectoryException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
    public companion object
}
