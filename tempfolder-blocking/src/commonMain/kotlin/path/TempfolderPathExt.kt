/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.path

internal fun TempfolderPathString.asStringOrDescription(): String = try {
    this.asString()
} catch (@Suppress("SwallowedException") ce: TempfolderCharacterCodingException) {
    this.bytes.toString()
}
