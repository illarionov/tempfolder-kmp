/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.ignore.annotations

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

public actual typealias IgnoreJs = kotlin.test.Ignore

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreAndroid actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreJvm actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreNative actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreWasmJs actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreApple actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreIos actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreMacos actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreLinux actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreMingw actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreWasmWasi actual constructor()
