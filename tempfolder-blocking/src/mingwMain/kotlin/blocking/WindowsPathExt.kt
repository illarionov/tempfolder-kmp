/*
 * SPDX-FileCopyrightText: 2024-2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.blocking

internal fun combinePath(root: String, child: String): String {
    if (child.isEmpty()) {
        return root
    }
    return if (root.endsWith("\\") || root.endsWith("/")) {
        """$root$child"""
    } else {
        """$root\$child"""
    }
}
