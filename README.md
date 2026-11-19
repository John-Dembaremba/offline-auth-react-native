
# offline-auth-react-native

A TypeScript-first React Native module for secure offline authentication on Android, powered by [offline-auth-android](https://github.com/John-Dembaremba/offline-auth-android) and the **Expo Modules API**.

`offline-auth-react-native` enables applications to synchronize encrypted user credentials while online and authenticate users locally when an internet connection is unavailable.

Passwords are verified by the native Android library, using Android Keystore-backed cryptography and encrypted local credential storage.

**Latest release: v0.1.1**

## Features

- Android Keystore-backed RSA key management.
- RSA-OAEP-SHA256 key unwrapping.
- AES-256-GCM credential encryption.
- Encrypted local credential storage.
- Django-compatible PBKDF2-SHA256 password verification.
- Multiple offline users per device.
- Offline credential synchronization and authentication.
- Credential expiration validation.
- Credential-version rollback protection.
- Authoritative snapshot synchronization.
- Partial-success synchronization with typed failures.
- Strongly typed TypeScript API.
- Expo config plugin for native Android configuration.
- Backend-agnostic offline authentication protocol.

## Compatibility

| Platform | Support |
|---|---|
| Android | Supported |
| Minimum Android API | 23 (native library) |
| Android compile SDK | 35+ |
| Expo SDK | 58 (previously verified) |
| React Native | 0.88.1 (previously verified) |
| iOS | Not supported |
| Web | Not supported |
| Expo Go | Not supported |

The native Android library uses Kotlin 2.2.10.

Applications using older Kotlin or Expo SDK versions should verify native build compatibility.

An Android development build or custom native build is required.

## Installation

Install the latest version:

```bash
npm install offline-auth-react-native@0.1.1
```

Or with Bun:

```bash
bun add offline-auth-react-native@0.1.1
```

### Expo Configuration

Add the module to your existing `app.json`:

```json
{
  "expo": {
    "plugins": [
      "offline-auth-react-native"
    ]
  }
}
```

Generate and build the native Android application:

```bash
npx expo prebuild --platform android
npx expo run:android
```

The Expo config plugin automatically enables the required Android core library desugaring.

### Native Android Dependency

The wrapper uses the Maven Central artifact:

```text
io.github.john-dembaremba:offline-auth-android:0.1.1
```

See [offline-auth-android on Maven Central](https://central.sonatype.com/artifact/io.github.john-dembaremba/offline-auth-android/0.1.1).

Applications generally do not need to declare this Maven dependency manually.

## Companion Backend Implementation

A compatible Django backend package is available:

### [django-offline-auth](https://github.com/John-Dembaremba/django-offline-auth)

`django-offline-auth` is an open-source Django package that implements the backend credential-issuing side of the offline authentication protocol.

It integrates with Django's authentication system and generates encrypted credential snapshots that can be synchronized with `offline-auth-react-native`.

The Django package provides:

- Offline authentication enablement and disablement.
- User-specific credential version management.
- Credential rotation.
- Django password verifier validation.
- AES-256-GCM credential encryption.
- RSA-OAEP-SHA256 AES key wrapping.
- Configurable offline credential expiration.
- Multi-user credential snapshot generation.
- Partial-success synchronization responses.
- Backend-side credential provisioning without permanent device registration.

The host Django application is responsible for authentication, authorization, selecting eligible users, and exposing the synchronization endpoint.

The Django package handles credential generation, encryption, versioning, and expiry.

### Backend Synchronization Example

The React Native application obtains its public key:

```typescript
import { getPublicKey } from 'offline-auth-react-native';

const publicKey = await getPublicKey();
```

A compatible Django endpoint accepts a request such as:

```json
{
  "public_key": "MIIBIjANBgkqh..."
}
```

The backend generates an encrypted credential snapshot for the authorized users.

The React Native application receives the snapshot and synchronizes it locally:

```typescript
import {
  sync,
  type OfflineCredentialSnapshot,
} from 'offline-auth-react-native';

// Fetch from your own authenticated backend endpoint.
const response = await fetch(
  'https://api.example.com/offline-auth/sync/',
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      // Include your application's authentication credentials.
    },
    body: JSON.stringify({ public_key: publicKey }),
  }
);

if (!response.ok) {
  throw new Error('Offline credential synchronization failed');
}

const snapshot: OfflineCredentialSnapshot =
  await response.json();

const result = await sync(snapshot);
```

**Note:** The endpoint URL above is illustrative. Applications must implement their own authenticated API endpoint. The Django package does not require a specific URL structure.

The backend must select eligible users through trusted application authorization rules, never from arbitrary user IDs supplied by the client.

### Backend Independence

Although `django-offline-auth` provides a reference backend implementation, this React Native module is not restricted to Django.

Any backend implementing the supported cryptographic protocol and credential snapshot format can be used.

## API Usage

### 1. Get Device Public Key

```typescript
import { getPublicKey } from 'offline-auth-react-native';

const publicKey = await getPublicKey();
```

Returns the device's RSA public key for backend credential encryption.

The private key remains under Android Keystore management and is not exported to JavaScript.

### 2. Synchronize Offline Credentials

```typescript
import {
  sync,
  type OfflineCredentialSnapshot,
} from 'offline-auth-react-native';

const snapshot: OfflineCredentialSnapshot =
  await fetchCredentialSnapshot();

const result = await sync(snapshot);

console.log(result.successfulUsernames);
console.log(result.failures);
```

`sync()` accepts a typed snapshot object and passes it to the native Android implementation.

### Credential Snapshot

```typescript
interface OfflineCredentialSnapshot {
  protocol_version: number;

  credentials: {
    user_id: string;
    username: string;

    envelope: {
      key_algorithm: string;
      content_algorithm: string;
      encrypted_key: string;
      nonce: string;
      ciphertext: string;
    };
  }[];

  failures: {
    user_id: string;
    username: string;
    reason: string;
  }[];
}
```

### Synchronization Result

```typescript
interface CredentialSyncResult {
  successfulUsernames: string[];

  failures: {
    username: string;
    source: 'local' | 'backend';
    reason: string;
    storedCredentialVersion: number | null;
    incomingCredentialVersion: number | null;
  }[];
}
```

Credential-level failures are reported individually. Errors preventing synchronization from completing reject the Promise.

### Synchronization Rules

The native Android library uses authoritative snapshots:

- Successfully processed credentials replace existing credentials.
- Backend failures preserve previously stored credentials.
- Local processing failures preserve previously stored credentials.
- Users absent from the authoritative snapshot are removed.
- Lower credential versions are rejected to prevent rollback.
- Other valid credentials can still synchronize when an individual credential fails.

### 3. Authenticate Offline

```typescript
import { authenticate } from 'offline-auth-react-native';

try {
  const authenticated = await authenticate(
    username,
    password
  );

  if (authenticated) {
    // Allow the authorized offline workflow.
  } else {
    // Invalid or unavailable offline credentials.
  }
} catch (error) {
  // Handle native authentication errors.
}
```

Returns `Promise<boolean>`:

- `true`: Local authentication succeeded.
- `false`: Local authentication failed.
- Rejected Promise: The operation could not complete.

Credentials must have been synchronized previously.

## End-to-End Architecture

```text
              DJANGO BACKEND
        (django-offline-auth)
                     |
                     | Select eligible users
                     | Generate credentials
                     | Encrypt with AES-256-GCM
                     | Wrap key using RSA-OAEP
                     |
                     v
             ENCRYPTED SNAPSHOT
                     |
                     v
             REACT NATIVE APP
          (offline-auth-react-native)
                     |
                     | Expo Modules API
                     v
              ANDROID LIBRARY
            (offline-auth-android)
                     |
              Android Keystore
              Encrypted Storage
              PBKDF2 Verification
                     |
                     v
             OFFLINE AUTHENTICATION
```

### Online Synchronization

```text
User authenticates online
        |
        v
getPublicKey()
        |
        v
Send public key to backend
        |
        v
Backend issues encrypted snapshot
        |
        v
sync(snapshot)
        |
        v
Android validates and stores credentials
```

### Offline Authentication

```text
Device is offline
        |
        v
User enters username and password
        |
        v
authenticate(username, password)
        |
        v
Native Android password verification
        |
        v
Authentication success or failure
        |
        v
Application enforces permitted offline access
```

## Supported Security Protocol

```text
Protocol version:    1
Key algorithm:       RSA-OAEP-256
Content algorithm:   A256GCM
Password algorithm:  pbkdf2_sha256
```

Unsupported algorithms and protocol versions are rejected by the native library.

## Security Considerations

- Android private keys remain managed by Android Keystore.
- Offline credentials are stored encrypted locally.
- Always use HTTPS for synchronization.
- Never log plaintext passwords or decrypted credential material.
- JavaScript strings cannot be reliably erased from memory.
- Passwords cross the JavaScript-to-native boundary.
- Offline authentication does not replace backend authorization.
- Account revocation and password changes may not be detected while a device remains offline.
- Enforce reasonable credential lifetimes and appropriate local access policies.
- Protect application backups and avoid exporting sensitive local storage.

Native asynchronous operations may reject with:

```text
ERR_OFFLINE_AUTH_CANCELLED
```

Cancellation rejection cannot be guaranteed to reach JavaScript after its runtime has been destroyed.

## Development and Testing

Clone the repository:

```bash
git clone https://github.com/John-Dembaremba/offline-auth-react-native.git

cd offline-auth-react-native

bun install
```

Run code-quality checks:

```bash
make check
```

Run JavaScript/TypeScript unit tests:

```bash
make test
```

Build the module:

```bash
bun run build
```

Run Android instrumentation tests with an emulator or device attached:

```bash
make integration-test
```

Run the complete suite:

```bash
make full-test
```

Inspect the npm package:

```bash
npm pack --dry-run
```

### Previously Verified Environment

| Test | Result |
|---|---|
| Jest tests | 15 passed |
| Android instrumentation tests | 13 passed on API 28 |
| Independent Expo SDK 58 installation | Passed |
| React Native 0.88.1 compilation | Passed |
| Native autolinking | Passed |
| Public-key retrieval runtime test | Passed |

These results are from the previous verified wrapper environment. Consumer builds using the updated v0.1.1 Maven dependency should be verified separately.

## Project Structure

```text
offline-auth-react-native/
├── android/
│   └── src/
│       ├── main/
│       └── androidTest/
├── src/
├── plugin/
├── example/
├── internal/
├── build/
├── app.plugin.js
├── expo-module.config.json
├── tsconfig.json
├── package.json
├── bun.lock
├── CHANGELOG.md
├── LICENSE
└── README.md
```

## Related Projects

### Backend

- [django-offline-auth](https://github.com/John-Dembaremba/django-offline-auth) — Django backend implementation for encrypted offline credential issuance.

### Native Android

- [offline-auth-android](https://github.com/John-Dembaremba/offline-auth-android) — Android Keystore, encrypted credential storage, synchronization, and local password verification.

### React Native

- [offline-auth-react-native](https://github.com/John-Dembaremba/offline-auth-react-native) — TypeScript and Expo Modules API wrapper for Android offline authentication.

## License

MIT — see [LICENSE](LICENSE).

## Author

John Dembaremba

[GitHub](https://github.com/John-Dembaremba)
