# Barely 1.2 shared Home acceptance record

This record captures the automated and emulator evidence for the shared Home release. Physical owner acceptance on the Galaxy S24 Ultra remains the final daily-driver confirmation after downloading the immutable GitHub asset.

## Identity

- Application ID: `app.usefriendly.barely`
- Version code: `16`
- Version name: `1.2.0`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)
- Compile Android: API 37.0

## Scope

- Command and Classic render the same shared Home composable and ranking pipeline.
- Both styles share the bottom prompt, local history, fuzzy app/shortcut results, wallpaper tint, opacity, and radius.
- Command retains floating Apps and Settings actions as a minimal single-surface experience.
- Classic hides those redundant actions and retains its existing Favorites/Widgets page on the left and All apps page on the right.
- Favorites and hosted widgets remain one composition on one page; this release adds no widget duplication, database, backend, or permission.
- The app-action sheet enables hidden and fully expanded states so a long press cannot request an unavailable Material 3 state.

## Automated gates

| Gate | Result |
| --- | --- |
| Unit suite | Pass locally, including the app-action sheet state contract. |
| Android lint | Pass locally with no errors. |
| Debug APK | Built and installed on the Android emulator. |
| Optimized APK and AAB | Pass: `scripts/verify-release.sh` completed with R8, resource shrinking, certificate verification, and checksums. |
| Manifest privacy | Unchanged: no Internet permission, analytics, account system, or new runtime permission. |

## Emulator runtime evidence

- Compared the pre-change Command Home and the shared implementation side by side at the same emulator viewport; layout, prompt, top actions, tint, and spacing remained visually consistent.
- Confirmed Classic now shows the same shared prompt and wallpaper treatment without floating top actions.
- Typed an app query in Classic and launched the best locally ranked result from the shared Home.
- Opened local recent-search history with the same continuous upward gesture used by Command.
- Swiped from Classic Home to All apps and Favorites/Widgets, confirming the existing pager surfaces remain intact.
- Long-pressed an app from All apps, Favorites, and a shared-Home result; each opened the fully expanded, internally scrollable action sheet without a fatal exception.
- Installed the production-signed 1.1.0 GitHub APK, completed onboarding, upgraded it in place to the production-signed 1.2.0 candidate, and confirmed version code 16 launched directly into the preserved Command Home without repeating onboarding.

## Physical acceptance still required

On the Galaxy S24 Ultra, upgrade the production-signed 1.1 APK to 1.2 and verify:

1. In Command, type and launch an app, open history, and confirm the top-right Apps and Settings actions remain available.
2. Switch to Classic and confirm the center Home uses the same prompt and visual settings but has no redundant top-right actions.
3. Swipe left and right from Classic Home and confirm All apps and Favorites/Widgets remain reachable and unchanged.
4. Long-press WhatsApp or another app with many published shortcuts from All apps, a Home result, and Favorites; scroll the sheet to its final action and dismiss it without a crash.
5. Restart Barely and confirm the chosen Home style, background, favorites, widgets, and local ranking history persist.
