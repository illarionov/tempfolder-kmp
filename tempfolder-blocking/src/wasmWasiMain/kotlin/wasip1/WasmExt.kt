/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(UnsafeWasmMemoryApi::class)

package at.released.tempfolder.wasip1

import kotlin.wasm.unsafe.MemoryAllocator
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi

internal const val WASM_POINTER_SIZE = 4

internal fun MemoryAllocator.allocateWasmPtr(): Pointer = allocate(WASM_POINTER_SIZE)
internal fun MemoryAllocator.allocateS32(): Pointer = allocate(4)

internal fun Pointer.loadPointer(): Pointer = loadInt().toUInt().let(::Pointer)
