/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.tempfolder.util

@Suppress("TooGenericExceptionCaught")
internal inline fun <R> runStackSuppressedExceptions(
    block: () -> R,
    finally: () -> Unit,
): R {
    var mainException: Throwable? = null
    return try {
        block()
    } catch (ex: Throwable) {
        mainException = ex
        throw ex
    } finally {
        if (mainException == null) {
            finally()
        } else {
            try {
                finally()
            } catch (ex: Throwable) {
                mainException.addSuppressed(ex)
            }
        }
    }
}
