# Barely 0.9 release-candidate record

This document records the automated and emulated evidence for the 0.9 trust and portability release candidate. It does not replace final owner acceptance on a physical Galaxy S24 Ultra.

## Candidate

- Application ID: `app.usefriendly.barely`
- Version code: `13`
- Version name: `0.9.0`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)
- Compile Android: API 36
- Release APK mode: R8 optimized and resource-shrunk

## Verified on 2026-07-21

| Area | Evidence | Result |
| --- | --- | --- |
| Unit and migration tests | JUnit debug suite | Pass: 23 tests, including legacy/current settings schemas, corrupt-value fallback, commands, ranking, widgets, and gestures. |
| Search latency | 1,000 synthetic launcher entries, 20 warmed samples | Pass: 14.57 ms measured p95 in the full suite against a deliberately conservative 250 ms host-side gate. |
| Static analysis | Android lint | Pass: 0 errors. Remaining warnings are pinned dependency/API/resource-maintenance notices rather than correctness failures. |
| Optimized artifacts | `assembleRelease` and `bundleRelease` with R8/resource shrinking | Pass: unsigned release APK was about 2.8 MB before signing, compared with the tool-heavy debug APK. |
| Signing pipeline | Ephemeral 3,072-bit RSA test key | Pass: environment-only Gradle signing produced APK/AAB, APK signature verification passed, and SHA-256 files were emitted. The ephemeral key was deleted and is not a Barely production key. |
| Upgrade | Android 16 emulator, existing 0.8 data | Pass: 0.9 installed over the matching GitHub test certificate, loaded the saved Command Home choice, migrated gestures, wrote settings schema 2, and survived process restart. |
| Optimized runtime | R8 APK re-signed with the existing GitHub testing certificate | Pass: app discovery, Command Home, All apps, Settings, stored enum choices, and process restart worked without keep-rule or reflection failures. |
| Expanded display | 2208 × 1840 px at 320 dpi | Pass: All apps used three balanced columns, the bottom search control stayed constrained, and content avoided system bars. |
| Reduced motion | all Android animation scales set to zero | Pass: Command to All apps and back remained usable. |
| Accessibility semantics | source and UI-tree review | Pass: major headings, Search and app-action panes, and Home/assistant radio choices expose explicit semantics; interactive edit controls retain 48 dp targets. |
| Privacy manifest | merged-manifest and source review | Pass: no Internet permission, cleartext disabled, backup/device transfer excluded, optional contacts, and system-bound notification/accessibility services. |
| Signing fallback | unsigned build without environment secrets | Pass: release APK is named `app-release-unsigned.apk`; Gradle does not substitute the debug key. |

## Release gates

Every candidate must pass a clean unit-test, lint, debug APK, optimized APK, and AAB build. A production candidate additionally requires `scripts/verify-release.sh`, the expected certificate fingerprint, SHA-256 comparison, and secure recovery copies of the permanent keystore.

## Owner acceptance still required

- Install the GitHub APK on the Galaxy S24 Ultra and verify the One UI Home-role picker and rollback to One UI Home.
- Check the owner's real wallpaper, Samsung keyboard Search action, gesture-access flow, WhatsApp shortcuts, and real widgets.
- Exercise optional notification/media access only if deliberately enabled.
- Review translations with native speakers before calling linguistic coverage final.
- Select and back up the permanent production key before a production-signed 1.0; changing from the current GitHub testing certificate requires one uninstall/reinstall outside Play App Signing.
