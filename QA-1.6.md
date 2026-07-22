# Barely 1.6 performance and daily-driver acceptance record

This record covers the generated optimization profile, reproducible performance journeys, local Favorite ordering, and the Android/One UI wallpaper fallback. Physical acceptance on the Galaxy S24 Ultra remains required after installing the immutable production-signed GitHub APK.

## Identity

- Application ID: `app.usefriendly.barely`
- Version code: `20`
- Version name: `1.6.0`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)
- Compile Android: API 37.0

## Scope

- The release APK embeds a generated Baseline Profile covering Home startup, Search entry, All apps entry, and first scroll. A focused startup profile covers initial Home composition.
- The `baselineprofile` module measures cold startup, warm Home resume, Search transition frames, and first-scroll frames with Android Macrobenchmark; `scripts/benchmark.sh` reproduces the suite locally.
- Favorites can be reordered with a drag handle and assistive move actions, or ranked by local launches. Shortcuts use the same ranking and never request Android Usage Access.
- Settings reports whether Android currently exposes native cross-window blur. When it does not, a local contrast slider tunes the wallpaper-derived fallback on every non-Home surface.
- App icons can retain their published silhouette or use a consistent Circle, Squircle, or Rounded square mask across every launcher surface; the choice remains local and defaults safely to Original on upgrade or corrupt input.

## Automated gates

| Gate | Local result |
| --- | --- |
| Baseline Profile generation | Pass: 20,562 baseline rules and a focused 18,957-rule startup profile were generated from the instrumented journeys. |
| Release profile packaging | Pass: optimized APK contains `assets/dexopt/baseline.prof` and `baseline.profm`. |
| Full unit suite | Pass: 32 tests across favorite ordering, settings, search, widgets, navigation, commands, history, and sheet behavior. |
| Android lint | Pass with no errors; only the deliberate toolchain/target notices remain. |
| Macrobenchmark instrumentation | Pass: all four 10-iteration journeys completed in one emulator smoke run. Emulator timings are deliberately not treated as physical performance evidence. |
| Debug and optimized artifacts | Pass locally for debug APK and unsigned optimized APK/AAB; merged-main CI and the signed workflow must reproduce the build and certificate gate. |

## Emulator acceptance

1. Start from a clean install, complete onboarding, open Search, return Home, open All apps, and perform the first scroll. Confirm the Baseline Profile generator completes and writes both profile files.
2. Add at least three app and shortcut favorites. Enter reorder mode from the Favorites header, drag each handle across another row, leave edit mode, restart Barely, and confirm the order persists.
3. Enable TalkBack in reorder mode and use Move favorite up/down. Confirm the focus target is 48 dp, the action is announced, and launch/long press remain disabled while editing.
4. Select Most used, launch an app and a published shortcut repeatedly through Barely, then return to Favorites and confirm the locally ranked order updates. Clear local history and confirm the ranking resets without removing favorites.
5. Disable system cross-window blur and verify Settings reports Translucent fallback. Adjust contrast from 0% to 100% and inspect Favorites, All apps, Command Apps, and full Search for continuous wallpaper visibility and readable foregrounds.
6. Re-enable blur and verify Settings reports Native Android blur; the fallback slider becomes informationally visible but disabled because Android owns the active renderer.
7. Repeat with 200% font, RTL, 0× animator scale, keyboard/DeX input, and an expanded foldable window. No essential action may clip or depend on motion.
8. Enable app icons and cycle through Original, Circle, Squircle, and Rounded square. Confirm the same mask appears in All apps, Home and full Search, recent history, Favorites, and app actions, then restart Barely and verify persistence.

## Galaxy S24 Ultra acceptance

1. Upgrade the signed 1.5 APK to 1.6 and verify Home role, favorites, widgets, gestures, and portable settings remain intact.
2. Toggle Samsung **Reduce transparency and blur**, power saving, and normal display mode. Record the renderer label shown by Barely in each state; the UI must never claim native blur while Android reports it disabled.
3. Tune fallback contrast over a bright and a dark wallpaper. Favorites, Apps, Search, and Command Apps must keep the wallpaper visible without returning to a flat gray surface.
4. Reorder mixed apps and WhatsApp shortcuts with touch and TalkBack, restart the launcher, and confirm persistence. Switch to Most used and verify only Barely launches affect order.
5. Run `scripts/benchmark.sh` on a development-connected S24 Ultra with a stable thermal state and save the JSON reports as the physical baseline for future releases.
6. Compare Xiaomi Home and other nonuniform third-party icons under all four masks. Squircle and Rounded square must clip square corners cleanly without changing icon scale, labels, touch targets, or shortcut-owner artwork.
