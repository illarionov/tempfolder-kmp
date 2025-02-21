# Tempfolder KMP

A library for creating temporary directories in Kotlin Multiplatform projects.

## Features

* Provides a unified API for temporary directory creation across most Kotlin Multiplatform targets.
* The *[TempDirectory]* extends *AutoCloseable* interface and requires calling `close()` after use.
* By default, the temporary directory is recursively when upon closing. Use the [deleteOnClose] property to disable
automatic deletion.
* Exposes the native file descriptor for integration with native filesystem functions, such as using `openat2()` on Linux.
* Strict error handling: all errors and exceptions are rethrown, including during `close()` if files cannot be deleted.
This helps to identify open file descriptor leaks.
* Supports platform-specific implementations that offer further configuration options for Android, iOS, Linux, macOS,
  and more.

## Installation

The latest release is available on [Maven Central]. Add the dependency:

```kotlin
dependencies {
    implementation("at.released.tempfolder:tempfolder-sync:<not yet released>")
}
```

## Usage

Below is an example of how to use the library with [kotlinx-io] in the *commonMain* source set:

```kotlin
import at.released.tempfolder.sync.createTempDirectory
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

fun main() {
    createTempDirectory().use { tempDirectory ->
        val absolutePath = tempDirectory.getAbsolutePath().asString()
        SystemFileSystem.sink(Path(absolutePath, "temp.txt")).buffered().use {
            it.writeString("Temp file")
        }
    }
}

```

## Platform implementations

There are several platform-specific implementations under a single [createTempDirectory] function in the common source set.
These implementations can also be used standalone, offering various extended settings that cover specific filesystem 
features and provide access to native file descriptors.

#### Android and JVM

* JVM Nio implementation: [createJvmTempDirectory()][createJvmTempDirectory]

#### Apple platform

* POSIX-based: [createAppleTempDirectory()][createAppleTempDirectory]

* NSFileManager-based: [createNsurlTempDirectory()][createNsurlTempDirectory] 

#### Windows

* Windows API-based: [createWindowsTempDirectory()][createWindowsTempDirectory]

#### Linux

* POSIX-based: [createLinuxTempDirectory()][createLinuxTempDirectory] 

#### Node.JS

* *node:fs*-based: [createNodeJsTempDirectory()][createNodeJsTempDirectory] 

#### Wasm WASI

* WASI Preview1: [createWasip1TempDirectory()][createWasip1TempDirectory]

#### Android Native

* POSIX-based: [createAndroidNativeTempDirectory()][createAndroidNativeTempDirectory]

## Contributing

Any type of contributions are welcome. Please refer to the [contribution guide] for more information.

## License

These services are licensed under Apache 2.0 License. Authors and contributors are listed in the
[Authors] file.

```
Copyright 2025 at-released-tempfolder project authors and contributors.

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

[Authors]: https://github.com/illarionov/tempfolder-kmp/blob/main/AUTHORS
[deleteOnClose]: https://tempfolder.released.at/tempfolder-sync/at.released.tempfolder.sync/-temp-directory/delete.html
[TempDirectory]: https://tempfolder.released.at/tempfolder-sync/at.released.tempfolder.sync/-temp-directory/index.html
[contribution guide]: https://github.com/illarionov/tempfolder-kmp/blob/main/CONTRIBUTING.md
[createAndroidNativeTempDirectory]: https://tempfolder.released.at/tempfolder-sync/at.released.tempfolder.sync/create-android-native-temp-directory.html
[createAppleTempDirectory]: https://tempfolder.released.at/tempfolder-sync/at.released.tempfolder.sync/create-apple-temp-directory.html
[createJvmTempDirectory]: https://tempfolder.released.at/tempfolder-sync/at.released.tempfolder.sync/create-jvm-temp-directory.html
[createLinuxTempDirectory]: https://tempfolder.released.at/tempfolder-sync/at.released.tempfolder.sync/create-linux-temp-directory.html
[createNodeJsTempDirectory]: https://tempfolder.released.at/tempfolder-sync/at.released.tempfolder.sync/create-node-js-temp-directory.html
[createNsurlTempDirectory]: https://tempfolder.released.at/tempfolder-sync/at.released.tempfolder.sync/create-nsurl-temp-directory.html
[createTempDirectory]: https://tempfolder.released.at/tempfolder-sync/at.released.tempfolder.sync/create-temp-directory.html
[createWasip1TempDirectory]: https://tempfolder.released.at/tempfolder-sync/at.released.tempfolder.sync/create-wasip1-temp-directory.html 
[createWindowsTempDirectory]: https://tempfolder.released.at/tempfolder-sync/tempfolder-sync/at.released.tempfolder.sync/create-windows-temp-directory.html
[kotlinx-io]: https://github.com/Kotlin/kotlinx-io
