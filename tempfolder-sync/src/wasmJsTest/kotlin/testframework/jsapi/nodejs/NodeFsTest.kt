/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:JsModule("node:fs")

package at.released.tempfolder.testframework.jsapi.nodejs

internal external fun statSync(path: String, options: JsAny): Stats?

internal external fun lstatSync(path: String, options: JsAny): Stats?

internal external fun symlinkSync(target: String, path: String, type: String?)

internal external fun writeFileSync(file: String, data: String, opts: JsAny)

internal external interface Stats : JsAny {
    var dev: Long
    var ino: Long
    var mode: Long
    var nlink: Long
    var uid: Long
    var gid: Long
    var rdev: Long
    var size: Long
    var blksize: Long
    var blocks: Long
    var atimeMs: Long
    var mtimeMs: Long
    var ctimeMs: Long
    var birthtimeMs: Long
    var atime: JsAny
    var mtime: JsAny
    var ctime: JsAny
    var birthtime: JsAny
    fun isBlockDevice(): Boolean
    fun isCharacterDevice(): Boolean
    fun isDirectory(): Boolean
    fun isFIFO(): Boolean
    fun isFile(): Boolean
    fun isSocket(): Boolean
    fun isSymbolicLink(): Boolean
}
