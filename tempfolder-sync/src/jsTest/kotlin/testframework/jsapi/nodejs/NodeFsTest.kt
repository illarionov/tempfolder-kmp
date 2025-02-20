/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:JsModule("node:fs")

package at.released.tempfolder.testframework.jsapi.nodejs

import org.khronos.webgl.ArrayBufferView
import kotlin.js.Date

internal external fun statSync(path: String, options: LstatOptions): Stats?

internal external fun lstatSync(path: String, options: LstatOptions): Stats?

internal external fun symlinkSync(target: String, path: String, type: String?)

internal external fun writeFileSync(file: String, data: ArrayBufferView, opts: WriteFileOptions)

internal external interface LstatOptions {
    var bigint: Boolean?
    var throwIfNoEntry: Boolean?
}

internal external interface WriteFileOptions {
    var encoding: String?
    var mode: Int?
    var flag: String?
    var flush: Boolean?
}

internal external interface Stats {
    var dev: Number
    var ino: Number
    var mode: Number
    var nlink: Number
    var uid: Number
    var gid: Number
    var rdev: Number
    var size: Number
    var blksize: Number
    var blocks: Number
    var atimeMs: Number
    var mtimeMs: Number
    var ctimeMs: Number
    var birthtimeMs: Number
    var atime: Date
    var mtime: Date
    var ctime: Date
    var birthtime: Date
    fun isBlockDevice(): Boolean
    fun isCharacterDevice(): Boolean
    fun isDirectory(): Boolean
    fun isFIFO(): Boolean
    fun isFile(): Boolean
    fun isSocket(): Boolean
    fun isSymbolicLink(): Boolean
}
