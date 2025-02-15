/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:JsModule("node:fs")

package at.released.tempfolder.jsapi.nodejs

internal external fun mkdirSync(path: String, mode: Int)

internal external fun realpathSync(path: String): String

internal external fun rmSync(path: String, options: RmSyncOptions)

internal external interface RmSyncOptions {
    var force: Boolean?
    var maxRetries: Int?
    var recursive: Boolean?
    var retryDelay: Int?
}
