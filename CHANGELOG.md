# Changelog

All notable changes to `offline-auth-react-native` will be documented in this file.

This project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Planned

- Publish the initial npm release.

## [0.1.0] - 2026-11-16

### Added

- Initial Android implementation using the Expo Modules API and Kotlin.
- Integration with `offline-auth-android:0.1.0` from Maven Central.
- TypeScript API for retrieving the Android device public key through `getPublicKey()`.
- Encrypted credential synchronization through `sync(snapshot)`.
- Offline user authentication through `authenticate(username, password)`.
- Strongly typed credential snapshot, synchronization result and failure interfaces.
- Expo config plugin for automatic Android core library desugaring.
- Native Promise cancellation handling and error propagation.
- Android instrumentation and JavaScript unit tests.
- Example React Native application and integration documentation.

### Security

- Android Keystore-backed cryptographic operations through the native library.
- Native password verification and encrypted credential storage.
- Credential-version rollback protection.
- Temporary Kotlin password character-array cleanup after authentication.
- Cancellation-aware handling of native asynchronous operations.

### Verified

- Expo SDK 58 and React Native 0.88.1.
- Android API 28 emulator.
- 15 Jest tests and 13 Android instrumentation tests.
- Independent npm tarball installation in an Expo application.
- Expo autolinking, Android compilation and runtime public-key retrieval.

[Unreleased]: https://github.com/John-Dembaremba/offline-auth-react-native/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/John-Dembaremba/offline-auth-react-native/releases/tag/v0.1.0
