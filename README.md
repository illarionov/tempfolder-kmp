# Tempfolder KMP

[![Maven Central](https://img.shields.io/maven-central/v/at.released.tempfolder/tempfolder-sync)][Maven Central]
[![build](https://github.com/illarionov/tempfolder-kmp/actions/workflows/Build.yml/badge.svg)](https://github.com/illarionov/tempfolder-kmp/actions/workflows/Build.yml)
[![kdoc](https://img.shields.io/badge/API_reference-KDoc-blue)](https://tempfolder.released.at)


A library for creating temporary directories in Kotlin Multiplatform projects.

## Installation

The latest release is available on [Maven Central]. Add the dependency:

```kotlin
dependencies {
    implementation("at.released.tempfolder:tempfolder-sync:0.1")
}
```

## Usage

Below is an example of how to use the library with [kotlinx-io] in the `commonMain` source set:

```kotlin
import at.released.tempfolder.sync.createTempDirectory
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

fun main() {
    createTempDirectory().use { tempDirectory ->
        val absolutePath = tempDirectory.absolutePath().asString()
        SystemFileSystem.sink(Path(absolutePath, "temp.txt")).buffered().use {
            it.writeString("Temp file")
        }
    }
}

```

## Features

* Provides a unified API for temporary directory creation across most Kotlin Multiplatform targets.
* The *TempDirectory* extends *AutoCloseable* and requires calling `close()` after use.
* The temporary directory is recursively deleted upon closing. This can be disabled by setting 
  `tempDirectory.deleteOnClose=false`.
* Exposes the native file descriptor for integration with native filesystem functions, 
  such as using `openat2()` on Linux.
* Strict error handling: all errors and exceptions are rethrown, including during `close()` if files cannot be deleted.
* Supports platform-specific implementations that offer further configuration options for Android, iOS, Linux, macOS,
  and more.

For more information, check the project API reference: [tempfolder.released.at](https://tempfolder.released.at)

[Maven Central]: https://central.sonatype.com/artifact/at.released.tempfolder/tempfolder-sync
[kotlinx-io]: https://github.com/Kotlin/kotlinx-io

## License

These services are licensed under Apache 2.0 License. Authors and contributors are listed in the
[Authors](AUTHORS) file.

```
Copyright 2025 Alexey Illarionov and at-released-tempfolder project contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
