# Module tempfolder-sync

Synchronous implementations of utilities for working with temporary directories.

The main functions for creating a directory are in the package [at.released.tempfolder.sync][at.released.tempfolder.sync]

# Package at.released.tempfolder.sync

Synchronous implementations of [TempDirectory].

Use [createTempDirectory] to create a temporary directory from common code or check platform-specific builders.

Platform implementations:

* JVM Nio implementation: [createJvmTempDirectory()][createJvmTempDirectory]
* Apple POSIX-based: [createAppleTempDirectory()][createAppleTempDirectory]
* Apple NSFileManager-based: [createNsurlTempDirectory()][createNsurlTempDirectory]
* Windows API-based: [createWindowsTempDirectory()][createWindowsTempDirectory]
* Linux POSIX-based: [createLinuxTempDirectory()][createLinuxTempDirectory]
* NodeJS *node:fs*-based: [createNodeJsTempDirectory()][createNodeJsTempDirectory]
* WASI Preview1: [createWasip1TempDirectory()][createWasip1TempDirectory]
* Android Native POSIX-based: [createAndroidNativeTempDirectory()][createAndroidNativeTempDirectory]

[at.released.tempfolder.sync]: https://tempfolder.released.at/tempfolder-sync/index.html
