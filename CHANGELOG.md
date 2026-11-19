
# Changelog

All notable changes to `offline-auth-react-native` will be documented in this file.

This project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.1.1] - 2026-11-19

### Changed

- Updated native Android dependency to `offline-auth-android:0.1.1`.
- Reduced the native library's minimum required compile SDK from 36 to 35.
- Updated installation and compatibility documentation.
- Added a companion backend implementation guide using `django-offline-auth`.
- Retained the existing public TypeScript API and Expo configuration plugin.

### Compatibility

- Android API 23+ supported by the native library.
- Android compile SDK 35+ required.
- Expo SDK 58 and React Native 0.88.1 previously verified.
- Compatibility with older Kotlin and Expo versions requires additional testing.

### Security

- Preserved Android Keystore-backed cryptography.
- Preserved encrypted credential synchronization and storage.
- Preserved offline password verification and credential rollback protection.
- No intentional changes to the credential protocol.

## [0.1.0] - 2026-11-16
```

Keep all your existing v0.1.0 sections underneath that heading.

At the bottom, use:

```markdown
[Unreleased]: https://github.com/John-Dembaremba/offline-auth-react-native/compare/v0.1.1...HEAD
[0.1.1]: https://github.com/John-Dembaremba/offline-auth-react-native/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/John-Dembaremba/offline-auth-react-native/releases/tag/v0.1.0
```

## 3. Commit and push directly to `main`

Assuming `git branch --show-current` confirms `main`:

```bash
cd ~/projects/offline-auth-react-native

git diff --check
git status --short

git add README.md CHANGELOG.md android/build.gradle package.json
git add -u

git diff --cached --stat

git commit -m "chore(release): prepare v0.1.1"

git push origin main
```

Before npm publishing, verify that the packed files reference version 0.1.1:

```bash
node -p "require('./package.json').version"

bun run build
npm pack --dry-run
```

Then publish through your configured npm authentication or trusted-publishing workflow, and verify:

```bash
npm view offline-auth-react-native@0.1.1 version
```

Once npm confirms the release, create the GitHub tag:

```bash
git tag -a v0.1.1 -m "Release offline-auth-react-native 0.1.1"
git push origin v0.1.1
```

**One important detail:** Calling v0.1.1 the latest release in your README is appropriate for the release commit, but npm will only serve it after publication succeeds. The README's installation command will work once that publication is complete.
