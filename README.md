# offline-auth-react-native

A reusable, TypeScript-first React Native module that provides offline authentication through the native Android library [`offline-auth-android`](https://github.com/John-Dembaremba/offline-auth-android).

Built using **Expo Modules API**, this package aims to expose secure Android offline authentication functionality to React Native applications through a simple, strongly typed TypeScript interface.

> **Project status:** Initial development (v0.1.0). The repository scaffold is complete; native bridge integration and authentication APIs are under development. Not yet published to npm.

## Overview

`offline-auth-react-native` allows React Native applications to integrate an offline authentication system without implementing the underlying Android cryptography and credential storage themselves.

The package will act as a bridge between React Native and the existing `offline-auth-android` library, which is published on Maven Central.

The native Android library provides:

- Android Keystore-backed cryptographic key management.
- RSA-OAEP-SHA256 key wrapping.
- AES-256-GCM encrypted credential handling.
- Encrypted local credential storage.
- Django-compatible PBKDF2-SHA256 password verification.
- Android API 23+ support.
- Credential snapshot synchronization.
- Credential-version rollback protection.
- Offline authentication without an active network connection.

The React Native module will expose these capabilities through an accessible TypeScript API.

## Architecture

```text
React Native Application
         |
         v
offline-auth-react-native
    TypeScript API
         |
         v
Expo Modules API
    Kotlin Bridge
         |
         v
offline-auth-android
    Maven Central
         |
         v
Android Keystore
Encrypted Credential Storage
PBKDF2 Password Verification
```

The React Native package is responsible for bridging JavaScript and native Android functionality. Cryptographic operations, credential verification, and persistent encrypted storage remain the responsibility of the native Android library.

## Platform Support

| Platform          | Status                          |
| ----------------- | ------------------------------- |
| Android (API 23+) | Native bridge under development |
| iOS               | Not supported                   |
| Web               | Not supported                   |

The initial release targets Android only.

## Technology Stack

| Component                      | Technology                              |
| ------------------------------ | --------------------------------------- |
| JavaScript-facing API          | TypeScript (strict mode)                |
| Native module framework        | Expo Modules API                        |
| Android implementation         | Kotlin                                  |
| Native authentication library  | `offline-auth-android`                  |
| Package manager                | Bun                                     |
| JavaScript testing             | Jest                                    |
| Native testing                 | Android instrumentation tests (planned) |
| Package distribution           | npm (planned)                           |
| Native dependency distribution | Maven Central                           |

## Planned API

The following API illustrates the intended functionality. It is not yet implemented.

### Create or retrieve the device public key

```typescript
import { getPublicKey } from 'offline-auth-react-native';

const publicKey = await getPublicKey();
```

The generated public key can be sent to a supported backend for encrypting credential information intended for the device.

### Synchronize offline credentials

```typescript
import { syncCredentials } from 'offline-auth-react-native';

const result = await syncCredentials(snapshotJson);
```

Synchronization will pass the backend-provided credential snapshot to the native Android library for verification, decryption, validation, and secure local storage.

The snapshot must follow the protocol supported by `offline-auth-android`.

### Authenticate offline

```typescript
import { authenticate } from 'offline-auth-react-native';

const result = await authenticate(username, password);
```

The native library will perform credential verification using locally stored encrypted credentials, without requiring an active internet connection.

The final TypeScript return types and error contracts will be defined as part of the bridge implementation.

## Security

The security-sensitive functionality resides in the Android library rather than being reimplemented in JavaScript.

The integration is designed around the following principles:

- Keep private cryptographic keys within Android Keystore.
- Avoid exposing decrypted credential snapshots to React Native.
- Perform password verification on the native Android side.
- Never log passwords, credential secrets, or decrypted authentication material.
- Validate credential snapshots before local synchronization.
- Preserve native credential-version and rollback protections.
- Return controlled authentication results and errors to JavaScript.

**JavaScript security limitation:** Passwords passed from React Native to Kotlin must cross the JavaScript-to-native boundary. JavaScript strings cannot be reliably erased from memory. Applications should avoid retaining passwords longer than necessary.

Offline authentication is not a substitute for server-side authorization. Applications must continue to enforce server-controlled permissions when communicating with protected backend services.

## Installation

The package is not yet available on npm.

Once published, the intended installation command will be:

```bash
bun add offline-auth-react-native
```

The module will require an Android native build and will not work in Expo Go without a custom development build containing the native module.

The underlying Android library is available on Maven Central:

```text
io.github.john-dembaremba:offline-auth-android:0.1.0
```

## Local Development

### Requirements

- Node.js and Bun
- Java Development Kit compatible with the Android Gradle project
- Android SDK
- Android emulator or physical Android device
- Expo and React Native development tooling

### Clone the repository

```bash
git clone https://github.com/John-Dembaremba/offline-auth-react-native.git
cd offline-auth-react-native
```

### Install dependencies

```bash
bun install
```

### Validate TypeScript

```bash
bunx tsc --noEmit
```

### Build the TypeScript package

```bash
bun run build
```

### Run linting

```bash
bun run lint
```

### Run tests

```bash
bun run test
```

### Example application

The `example/` directory contains the generated example React Native application, which will be extended to demonstrate native integration and offline authentication.

## Project Structure

```text
offline-auth-react-native/
├── android/                    # Kotlin Expo module
├── src/                        # Strict TypeScript API
├── example/                    # React Native example application
├── internal/                   # Generated module build scripts
├── expo-module.config.json     # Expo module registration
├── tsconfig.json               # TypeScript configuration
├── package.json                # Package metadata
├── bun.lock                    # Bun dependency lockfile
├── CHANGELOG.md
├── LICENSE
└── README.md
```

## Development Roadmap

- [x] Create standalone Expo module repository.
- [x] Configure Bun package management.
- [x] Configure strict TypeScript.
- [ ] Integrate the Maven Central Android dependency.
- [ ] Implement the Kotlin Expo module.
- [ ] Define typed credential synchronization and authentication results.
- [ ] Implement the public TypeScript API.
- [ ] Handle unsupported platforms.
- [ ] Implement unit and native integration tests.
- [ ] Validate the module using the example Android application.
- [ ] Document production integration.
- [ ] Publish the package to npm.

## Related Project

[**offline-auth-android**](https://github.com/John-Dembaremba/offline-auth-android)

The underlying Android authentication library, published to Maven Central, handles cryptographic operations, credential storage, synchronization, and offline password verification.

## License

MIT License. See [LICENSE](LICENSE).

## Author

John Dembaremba

[GitHub](https://github.com/John-Dembaremba)
