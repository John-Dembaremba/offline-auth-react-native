# offline-auth-react-native

A TypeScript-first React Native module for secure offline authentication on Android, powered by [offline-auth-android](https://github.com/John-Dembaremba/offline-auth-android) and the **Expo Modules API**.

The library allows applications to synchronize encrypted credentials and authenticate users locally without an internet connection.

> **Status:** v0.1.0 release candidate — implemented and tested, pending npm publication.

## Features

- Android Keystore-backed key management.
- RSA-OAEP-SHA256 and AES-256-GCM credential encryption support.
- Encrypted local credential storage.
- Django-compatible PBKDF2-SHA256 password verification.
- Offline credential synchronization and authentication.
- Credential-version rollback protection.
- Strongly typed TypeScript API.
- Expo config plugin for automatic Android setup.

## Compatibility

| Platform | Support |
|---|---|
| Android | Supported; tested on API 28 |
| Expo SDK | 58 (verified) |
| React Native | 0.88.1 (verified) |
| iOS / Web | Not supported |
| Expo Go | Not supported |

An Android native build is required. Other Expo and React Native versions have not yet been verified.

## Installation

Install after npm publication:

```bash
npm install offline-auth-react-native
```

Or:

```bash
bun add offline-auth-react-native
```

Add the Expo config plugin to your existing `app.json`:

```json
{
  "expo": {
    "plugins": ["offline-auth-react-native"]
  }
}
```

Then build the Android application:

```bash
npx expo prebuild --platform android
npx expo run:android
```

The config plugin automatically enables the required Java desugaring.

**Native dependency:** `io.github.john-dembaremba:offline-auth-android:0.1.0` (Maven Central).

## API Usage

### 1. Get the device public key

```typescript
import { getPublicKey } from 'offline-auth-react-native';

const publicKey = await getPublicKey();
```

Returns the public key used by a compatible backend to prepare encrypted credentials for the device. The private key remains managed by Android Keystore.

### 2. Synchronize offline credentials

```typescript
import {
  sync,
  type OfflineCredentialSnapshot,
} from 'offline-auth-react-native';

// Snapshot received from your compatible backend.
const snapshot: OfflineCredentialSnapshot = await fetchCredentialSnapshot();

const result = await sync(snapshot);

console.log(result.successfulUsernames);
console.log(result.failures);
```

`sync()` accepts a typed snapshot object, serializes it internally, and passes it to the native Android library.

The backend must supply a snapshot using the supported protocol.

**Snapshot structure:**

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

**Synchronization result:**

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

Individual credential failures are reported in the result. Errors preventing synchronization from completing reject the Promise.

### 3. Authenticate offline

```typescript
import { authenticate } from 'offline-auth-react-native';

try {
  const authenticated = await authenticate(username, password);

  if (authenticated) {
    // Allow the permitted offline workflow.
  } else {
    // Invalid or unavailable offline credentials.
  }
} catch (error) {
  // Handle native authentication errors.
}
```

Returns `Promise<boolean>`:

- `true`: Local verification succeeded.
- `false`: Local verification was unsuccessful.
- Rejected Promise: The operation could not complete.

Credentials must have been synchronized previously.

## Integration Flow

```text
Online authentication
        |
        v
getPublicKey()
        |
        v
Backend encrypts credentials for device
        |
        v
Receive encrypted credential snapshot
        |
        v
sync(snapshot)
        |
        v
Credentials stored securely on Android
        |
        v
Offline login
        |
        v
authenticate(username, password)
```

Your backend is responsible for provisioning compatible credential snapshots and enforcing server-side authorization.

## Error Handling and Security

Native methods can reject Promises when operations fail. Asynchronous native operations also support cancellation handling with the error code:

`ERR_OFFLINE_AUTH_CANCELLED`

Security considerations:

- Private keys remain under Android Keystore management.
- Credential decryption, storage and password verification occur in the native Android library.
- Never log passwords, encrypted credential snapshots or decrypted secrets.
- Passwords cross the JavaScript-to-native boundary; JavaScript strings cannot be reliably erased from memory.
- Offline authentication does not replace backend authorization or account revocation policies.
- Applications should handle expired, revoked or unavailable offline credentials according to their security requirements.
- Cancellation rejection cannot be guaranteed to reach JavaScript after the JavaScript runtime has been destroyed.

## Development and Testing

```bash
git clone https://github.com/John-Dembaremba/offline-auth-react-native.git
cd offline-auth-react-native

bun install
bun run build

make check
make unit-test
make integration-test
```

Android instrumentation tests require an emulator or connected Android device.

## Project Structure

```text
offline-auth-react-native/
├── android/
│   └── src/
│       ├── main/                # Kotlin Expo module
│       └── androidTest/         # Android instrumentation tests
├── src/                         # Public TypeScript API and tests
├── plugin/                      # Expo config plugin
├── example/                     # React Native example application
├── internal/                    # Module development scripts
├── app.plugin.js                # Expo plugin entrypoint
├── expo-module.config.json     # Native module registration
├── tsconfig.json
├── package.json
├── bun.lock
├── CHANGELOG.md
├── LICENSE
└── README.md
```

**v0.1.0 verification:**

| Test | Result |
|---|---|
| Jest | 15 passed |
| Android instrumentation | 13 passed on API 28 |
| Independent Expo SDK 58 installation | Passed |
| Kotlin compilation and native autolinking | Passed |
| Independent `getPublicKey()` runtime test | Passed |

## Architecture

```text
React Native / TypeScript
          |
    Expo Modules API
          |
      Kotlin Bridge
          |
  offline-auth-android
          |
      Android Keystore
  Encrypted Credential Storage
  PBKDF2 Password Verification
```

## License

MIT — see [LICENSE](LICENSE).

## Author

John Dembaremba

[GitHub](https://github.com/John-Dembaremba)
