/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

import at.released.tempfolder.TempfolderRuntimeException

/**
 * Исключение, выбрасываемое при кодировании или декодировании пути в UTF строку.
 */
public class TempfolderCharacterCodingException : TempfolderInvalidPathException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
}

/**
 * Исключение, выбрасываемое, если в пути встречается недопустимый для данной файловой системы символ.
 * Путь при этом может быть валидной UTF строкой.
 */
public class TempfolderInvalidCharacterException : TempfolderInvalidPathException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
}

/**
 * Строка пути пустая.
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
 * Исключения при обработке путей
 */
public open class TempfolderInvalidPathException : TempfolderRuntimeException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable) : super(cause)
    public constructor(message: String, cause: Throwable?) : super(message, cause)
    public companion object
}
