# Barely release, upgrade, and rollback

This document is the operator checklist for stable Barely builds. User-facing install steps remain in the README.

## Identity

- Application ID: `app.usefriendly.barely`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)
- The signing certificate, application ID, and monotonically increasing version code together define a valid Android upgrade.

## Reproducible verification

1. Start from a clean, reviewed `main` commit and a JDK 17 environment.
2. Verify `gradle/wrapper/gradle-wrapper.jar` against the checksum in CI and keep `distributionSha256Sum` in `gradle-wrapper.properties`.
3. Export the four `BARELY_*` signing variables described in `SECURITY.md`.
4. Run `scripts/verify-release.sh`.
5. Verify the APK certificate fingerprint against the offline release record.
6. Record the APK and AAB SHA-256 values in the GitHub release notes before upload.
7. Install the exact APK over the previous production-signed version and repeat the physical-device acceptance checklist.

## Upgrade behavior

Barely keeps settings in private `SharedPreferences` and widgets in Android's `AppWidgetHost`. Version 1.x reads the legacy boolean gestures, the 0.8 action enums, and settings schema 2. Unknown or corrupt values fall back to bounded local defaults instead of blocking Home.

An ordinary upgrade preserves the default Home role, favorites, widgets, appearance, optional-module switches, and local ranking history. Android permissions and profile visibility remain controlled by Android and device policy.

Changing the signing certificate is not an upgrade. Testers moving from the debug-signed 0.9 GitHub APK to the permanent production-signed 1.0 APK must first switch Home back to One UI Home or another launcher, uninstall Barely, install 1.0, select Barely as Home again, and re-add widgets. A portable settings export can restore non-sensitive appearance, gesture, search, and module preferences; it intentionally cannot restore widget IDs, permissions, favorites, profiles, or private history.

## Rollback

Android does not normally allow installing a lower version code over a newer build, and `adb install -d` is not a supported daily-driver rollback path. Before rollback:

1. Export portable settings if the current version still opens.
2. Switch the default Home app away from Barely.
3. Uninstall Barely, install the older APK signed by the expected key, and select it as Home.
4. Import only settings files whose schema that older version supports, then re-add widgets and optional permissions manually.

Keep the previous known-good APK and checksum attached to its immutable GitHub release. Never overwrite release assets or reuse a tag.

## Signing-key recovery

The permanent keystore and passwords must exist in separate encrypted backups controlled by the owner. Record the certificate SHA-256 fingerprint offline and in release notes. Test both recovery copies before publishing 1.0. Losing the key prevents direct upgrades; leaking it requires key revocation and a Play App Signing key-upgrade process where available.
