/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.jsapi.nodejs

/**
 * NodeJS [Error.code](https://nodejs.org/docs/latest/api/errors.html#errorcode) error code
 */
internal val Throwable.nodeJsErrorCode
    get() = asDynamic().code
