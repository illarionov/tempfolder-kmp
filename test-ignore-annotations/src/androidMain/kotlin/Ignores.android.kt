/*
 * SPDX-FileCopyrightText: 2025 Alexey Illarionov and the at-released-tempfolder project contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.ignore.annotations

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

public actual typealias IgnoreAndroid = org.junit.Ignore

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreApple actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreJs actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreJvm actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreNative actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreWasmJs actual constructor()

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
