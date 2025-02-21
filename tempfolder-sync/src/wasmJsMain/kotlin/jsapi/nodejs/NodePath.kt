/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:JsModule("node:path")

package at.released.tempfolder.jsapi.nodejs

internal external fun join(vararg parts: String): String
