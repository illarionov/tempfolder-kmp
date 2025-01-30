/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

import kotlin.random.Random

private const val MAX_RANDOM_NAME_NUMBER = 36L * 36L * 36L * 36L * 36L * 36L

@Suppress("MagicNumber")
internal fun generateTempDirectoryName(prefix: String): String {
    return prefix + Random.nextLong(until = MAX_RANDOM_NAME_NUMBER).toString(36).padStart(6, '0')
}
