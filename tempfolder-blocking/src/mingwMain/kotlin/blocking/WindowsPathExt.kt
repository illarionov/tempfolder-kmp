/*
 * Copyright 2024-2025, Alexey Illarionov and the at-released-tempfolder project contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
