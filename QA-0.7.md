# Barely 0.7 acceptance record

This document separates automated/emulated evidence from the physical-device approval required before publishing 0.7.

## Candidate

- Application ID: `app.usefriendly.barely`
- Version code: `8`
- Candidate version: `0.7-rc1`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)
- Release state: GitHub prerelease for physical approval; debug-signed and not stable
- Approval APK: `Barely-0.7-rc1.apk`
- SHA-256: `9972619a9055d2319beb9d26aaa82b499dc2eb7e06b5c2cd294f040d369376a0`

## Verified on 2026-07-21

| Area | Environment | Result |
| --- | --- | --- |
| Phone layout | Android 16 emulator, 1344 × 2992 px at 480 dpi | Pass: Terminal controls stay clear of system bars and the prompt remains bottom-reachable. |
| Expanded/foldable layout | 2208 × 1840 px at 320 dpi | Pass: All apps uses three balanced columns; the Terminal prompt is centered and constrained to 600 dp. |
| DeX-like window | 1920 × 1080 px at 240 dpi | Pass: prompt width is constrained; Down/Up changes the selected result; Enter launches the selected app; Escape clears the query. |
| Reduced motion | all three Android animator scales set to `0` | Pass: Terminal, All apps, Search, back, and app launch remained functional. |
| RTL and large text | Arabic at 150% font scale, 1344 × 2992 px | Pass: layout mirrors without clipping; literal Terminal tokens remain LTR and do not reverse their punctuation. |
| TalkBack smoke test | Google TalkBack service on the Android 16 emulator | Pass: swipe navigation produced a visible focus target on the 48 dp top command; localized content descriptions were exposed. |
| Localization packaging | English plus Arabic, German, Spanish, French, Hindi, Indonesian, Italian, Japanese, Korean, Brazilian Portuguese, Russian, and Simplified Chinese | Pass: Terminal and widget string-key parity plus XML validation. Human linguistic sign-off is still recommended before 1.0. |
| Privacy manifest | merged source manifest review | Pass: no Internet permission, backup disabled, contacts optional, notification/accessibility services system-bound. |
| Unit tests | JUnit debug suite | Pass: 15 tests across command parsing, search/ranking, widget packing, and appearance defaults. |
| Android build | `testDebugUnitTest assembleDebug lintDebug` with JDK 17 | Pass. The only compiler notice is the upstream deprecation of `rememberModalBottomSheetState`. |

During the expanded-window audit, two release-blocking defects were found and fixed: the Terminal prompt ignored its maximum readable width, and Terminal Home content remained beneath All apps. The RTL pass also fixed mirrored command punctuation.

## Physical Galaxy S24 Ultra approval — required before release

Install the approval APK and complete the README test checklist, with special attention to Samsung-specific behavior that an emulator cannot prove:

- One UI Home-role selection and recovery back to One UI Home.
- Wallpaper blur/fallback appearance with the owner's real light and dark wallpapers.
- Samsung keyboard one-handed Search action and pull-down dismissal.
- WhatsApp/other installed-app shortcuts through `LauncherApps.startShortcut`.
- Double-tap lock and notification shade through the optional, content-blind accessibility service.
- Real provider widgets: preview, configuration, live resize, side-by-side packing, persistence, and removal.
- Private Space/work-profile visibility and lock behavior if configured.
- Notification/media modules only if the owner explicitly opts in.
- A final visual check of Classic Home, Terminal, Favorites, Apps, Search, Settings, onboarding, widgets, and long app-action sheets.

Stable `v0.7` publication remains intentionally gated on explicit physical-device approval. Test candidates may be published only as clearly marked prereleases.
