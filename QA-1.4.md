# Barely 1.4 command and visual-consistency acceptance record

This record captures the automated and emulator evidence for quick favorites, shared command/search geometry, icon-only grids, and resilient wallpaper treatment. Physical owner acceptance on the Galaxy S24 Ultra remains required after downloading the immutable GitHub APK.

## Identity

- Application ID: `app.usefriendly.barely`
- Version code: `18`
- Version name: `1.4.0`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)
- Compile Android: API 37.0

## Scope

- App and published-shortcut results expose one trailing favorite action; long press opens the owner app actions.
- Home and All apps render the same 64 dp command/search container with one outline, tint, global radius, and clipped pressed state.
- Grid users who enable icons may independently hide visual labels; accessibility semantics retain every app name.
- The history clear action keeps a 48 dp target and gains radius-aware trailing inset.
- Cross-window blur flags and radius update in one window-attribute transaction. When Android disables blur, Barely uses a lighter translucent fallback and reports that state in Settings.

## Automated gates

| Gate | Result |
| --- | --- |
| Settings migration tests | Pass for the new label preference, existing upgrades, and corrupt-value recovery. |
| Full unit suite | Pass locally before publication. |
| Debug APK | Built and installed on the Android emulator. |
| Resource packaging | Pass for all 13 supported locales. |
| Optimized signed APK and AAB | Must pass in the signed release workflow before publication. |

## Emulator runtime evidence

- Compared the supplied Galaxy command-results screenshot with the 1.4 result row at the same interaction state.
- Confirmed the star toggles between Add to favorites and Remove from favorites without launching the result.
- Long-pressed a command result and confirmed the app action sheet opens without a crash.
- Confirmed All apps and Home use the same 64 dp bottom surface, border, opacity, and radius.
- Enabled Grid, Show app icons, and disabled Show grid labels; confirmed a balanced 4 × 6 icon-only grid with accessible app names.
- Confirmed the no-blur fallback retains more wallpaper color instead of presenting as a flat gray layer.

## Physical acceptance still required

On the Galaxy S24 Ultra, upgrade the production-signed 1.3 APK to 1.4 and verify:

1. Search for WhatsApp contacts and favorite both an app and a published shortcut from the trailing star.
2. Long-press the same rows and scroll the complete WhatsApp shortcut sheet.
3. Compare Home and All apps bottom controls at minimum, default, and maximum global radius.
4. Enable the icon-only grid and confirm long labels remain available to TalkBack.
5. Toggle Samsung's Reduce transparency and blur setting and confirm Barely changes between real blur and the intentional translucent fallback.
