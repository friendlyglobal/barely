# Barely security and privacy

Barely is designed to be a local launcher, not a data service. The application declares no Internet permission, contains no analytics or advertising SDK, has no account system, and disables Android backup and device-to-device transfer.

## Trust boundaries

- `LauncherApps` supplies apps, profiles, and shortcuts. Android controls which profiles and shortcuts Barely may see.
- `AppWidgetHost` renders provider-owned widget views. Barely stores widget IDs and layout metadata, never widget contents.
- Contact search is disabled until the user grants `READ_CONTACTS`. Matching happens in memory and contacts are not persisted.
- The optional notification listener can see sensitive notification metadata while Android grants access. Barely derives notification counts and current media state in memory and does not index or persist notification content.
- The optional accessibility service cannot retrieve window content and subscribes to no events. It performs only the explicit lock-screen and notification-shade global actions selected by the user.
- AI fallback appears only after local search has no result. A tap shares the typed question directly with the selected installed assistant app; Barely has no AI API key, proxy, or response access.
- Private Space and managed-profile isolation remain Android security boundaries. Hiding an ordinary app is organization, not security.

## Persisted local data

Barely uses private `SharedPreferences` for favorites, widget placement metadata, appearance, optional-module switches, and successful local query-to-app or shortcut associations. Portable settings export deliberately excludes widget IDs, permissions, profiles, favorites, and local history.

Settings decoding is versioned, bounds numeric values, rejects unsupported portable schemas, and safely falls back when enum or preference values are corrupt. Release tests cover the legacy gesture migration and current settings schema.

## Release signing

The Gradle release build never falls back to the debug key. It signs only when all four environment variables are present:

- `BARELY_KEYSTORE_FILE`
- `BARELY_KEYSTORE_PASSWORD`
- `BARELY_KEY_ALIAS`
- `BARELY_KEY_PASSWORD`

Run `scripts/verify-release.sh` to build, test, lint, verify the APK signature, and emit APK/AAB checksums. Keep the keystore and its recovery material in separate encrypted backups. Losing the signing key prevents updates to existing installations; changing it requires a new application identity or a supported Play App Signing key upgrade.

Barely 1.0 uses the permanent certificate SHA-256 `AF:B1:8D:2A:20:0A:0B:D8:27:30:33:CC:14:74:86:2B:86:84:70:B8:74:A8:96:A1:E7:D3:D4:99:C5:46:DD:48`. The local JKS is outside the repository with mode 600, its password is stored in macOS Keychain, and the manual signed-release workflow uses encrypted GitHub Actions secrets. None of those secrets are present in source, pull-request CI, logs, or release assets.

## Reporting a vulnerability

Please use GitHub's private **Security advisories > Report a vulnerability** flow for `friendlyglobal/barely`. Do not include contacts, notification contents, exported settings, keystores, passwords, or other personal data in a public issue.

Include the Barely version, Android version, device/profile setup, reproduction steps, and the security impact. Ordinary crashes and visual bugs can use the public issue tracker when the report contains no sensitive information.
