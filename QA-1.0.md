# Barely 1.0 stable acceptance record

This record freezes the technical evidence for Barely 1.0. Physical owner acceptance on the Galaxy S24 Ultra remains the final daily-driver confirmation after downloading the immutable GitHub asset.

## Identity

- Application ID: `app.usefriendly.barely`
- Version code: `14`
- Version name: `1.0.0`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)
- Compile Android: API 37.0, required by the pinned 2026 Compose toolchain
- Release mode: R8 optimized, resource-shrunk, and production-signed only when all environment secrets are present

## Automated gates

| Gate | Result |
| --- | --- |
| Unit and migration suite | Pass locally: 23 tests covering settings schemas, corrupt values, command parsing, search ranking/latency, widgets, and gestures; pending identical merged-main CI confirmation. |
| Android lint | Pass locally: 0 errors and 2 deliberate notices for the Android 16 target and tested Gradle 9.3.1 pin; pending merged-main CI confirmation. |
| Debug APK | Built locally. |
| Optimized APK and AAB | Built locally without a signing fallback. |
| Fuzzy-search gate | Pass locally: 11.71 ms p95 in the final full suite; threshold remains p95 under 250 ms for 1,000 synthetic launcher entries. |
| Locale resource parity | Pass: English plus all twelve localized resource sets have identical string keys; Indonesian uses Android's `values-in` qualifier. |
| Supply-chain controls | Pass: official Gradle 9.3.1 wrapper JAR and distribution SHA-256 values are pinned; GitHub Actions use reviewed immutable commit SHAs; Dependabot is enabled. |
| Manifest privacy | Pass: no Internet permission, cleartext disabled, cloud backup/device transfer excluded, and only documented optional/system launcher permissions remain. |
| Release artifact identity | Production signing passed locally with the permanent 4,096-bit key; final merged-main APK/AAB checksums and GitHub asset verification remain pending. |

## Upgrade and runtime acceptance

Version 1.0 installed over 0.9 using the testing certificate and preserved Command Home plus the saved gesture choices. A clean install signed by the permanent production certificate completed onboarding and entered Command Home without a runtime or R8 failure. The signing-certificate transition itself cannot be an in-place Android upgrade and is documented in `RELEASE.md`.

The final device pass covers Command and Classic Home, the Portuguese `Comando` label, All apps, local and AI-fallback search, long app-action sheets, favorite shortcuts, widgets, configurable gestures, optional sensitive access, reduced motion, RTL/large text, expanded layout, and rollback to One UI Home.

## Production signing evidence

- Certificate subject: `CN=Barely, OU=Usefriendly, O=Friendly Global, L=Brasilia, ST=Distrito Federal, C=BR`
- Certificate SHA-256: `AF:B1:8D:2A:20:0A:0B:D8:27:30:33:CC:14:74:86:2B:86:84:70:B8:74:A8:96:A1:E7:D3:D4:99:C5:46:DD:48`
- APK SHA-256: pending
- AAB SHA-256: pending
- Git tag and merged commit: pending
- Recovery state: local JKS is mode 600, password is in macOS Keychain, and the operational encrypted copy is in four GitHub Actions secrets. A separate owner-controlled offline backup remains recommended.
