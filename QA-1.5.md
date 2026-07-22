# Barely 1.5 responsiveness and polish acceptance record

This record captures automated and emulator evidence for immediate Home readiness, adaptive app-action sheets, compact All apps chrome, consistent search geometry, wallpaper-aware fallbacks, and predictive back. Physical owner acceptance on the Galaxy S24 Ultra remains required after downloading the immutable production-signed GitHub APK.

## Identity

- Application ID: `app.usefriendly.barely`
- Version code: `19`
- Version name: `1.5.0`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)
- Compile Android: API 37.0

## Scope

- Home requests only move the Classic pager when it is not already centered. Returning from an app keeps the current Home snapshot visible and moves widget/access metadata refresh away from the first interaction frame.
- Short app-action sheets wrap their content; long shortcut collections remain capped and internally scrollable. Search focus and the keyboard are dismissed before a sheet opens.
- All apps uses a compact Material 3 title, count, and settings action inside the lazy collection, so the header scrolls away while the bottom search entry remains available.
- Command, All apps, and full Search use the same 64 dp outlined input geometry. Full Search results, recommendations, and recent queries obey the configured global radius and expose the same optional favorite action.
- Page and search fallbacks blend a restrained tint from the current wallpaper instead of falling back to uniform gray when Android disables cross-window blur.
- Search, Settings, and Command Apps participate in Android predictive back. Settings exposes a modal-pane accessibility announcement.

## Automated gates

| Gate | Result |
| --- | --- |
| Home return policy | Pass: no reset on the center page; reset required from Favorites or Apps. |
| Full unit suite | Pass locally: 29 tests across eight suites, with 0 failures, 0 errors, and 0 skipped. Merged-main CI must reproduce it before publication. |
| Android lint | Pass locally with no errors and only the two deliberate toolchain/target notices already documented for 1.0. Merged-main CI must reproduce it before publication. |
| Debug APK | Built and installed on the Android emulator. |
| Optimized APK and AAB | Must pass the merged-main and signed release workflows. |
| Signing identity | Must match Barely's permanent production certificate before publication. |

## Emulator runtime evidence

- Installed the exact debug build on a 1344 × 2992 Android emulator and selected Barely as Home.
- Confirmed the compact All apps header and count are aligned at the top, then scrolled until the complete header left the viewport while the bottom search entry stayed reachable.
- Opened full Search and confirmed its 64 dp outlined field, leading icon, text baseline, result radius, and favorite targets match the shared command surfaces.
- Long-pressed Calendar from Search and confirmed focus cleared, the keyboard closed, and the short action sheet wrapped content instead of occupying 90% of the display.
- Opened Favorites with no saved entries and confirmed the copy explains both search starring and app long press, with a visible Add widget button.
- Set system font scale to 200% and confirmed All apps remained navigable with no clipped action target; restored the default scale afterward.
- Set all Android animation scales to 0× and repeated Home, Apps, Search, and Back navigation without a crash or motion-dependent dead end; restored animation scales afterward.
- Resized the emulator to a 2208 × 1840 expanded window and confirmed the same All apps renderer changed to a balanced two-column collection without clipping the compact header or bottom search entry.
- Repeated app-to-Home swipes at multiple delays. Barely no longer schedules its own center-page spring; the emulator still imposed a SystemUI-controlled Home handoff before third-party launcher gestures were accepted.

## Galaxy S24 Ultra acceptance

Upgrade the production-signed 1.4 APK to 1.5 and verify:

1. Press Home from WhatsApp, Chrome, and Settings, then immediately swipe to Apps and Favorites. Confirm Barely never replays a center-page spring or shows a loading indicator. Compare Samsung gesture navigation with three-button navigation and record any remaining SystemUI handoff delay separately.
2. Long-press Calendar or another short-action app from full Search; the keyboard must close and the sheet should wrap its content. Repeat with WhatsApp; all shortcuts, Favorite, App info, and Uninstall must remain reachable by internal scroll.
3. Scroll through the full 168-app collection. The title/count/settings header must scroll away, and the bottom Search surface must keep the same height, border, radius, and bottom spacing as the Home Command Bar.
4. Search for an app and a WhatsApp shortcut, toggle their trailing stars, reopen Favorites, and confirm both entries launch correctly.
5. Test the configured global radius at 0, 12, and 32 dp across Command, full Search input/results, recent queries, and All apps search.
6. Toggle Samsung **Reduce transparency and blur**, power saving, and the normal display mode. Confirm native blur appears when Samsung exposes it and the wallpaper-derived fallback stays legible without becoming a flat gray page.
7. Edge-swipe back from Search, Settings, and Command Apps. Confirm the surface follows the gesture and survives a cancelled back gesture.
8. Test 200% font size, TalkBack, RTL, animation scale 0×, split screen, DeX, and an unfolded layout. Confirm 48 dp action targets remain reachable and no header or sheet clips essential content.
9. Add, resize, interact with, reorder, and remove two real scrollable widgets on the single Favorites page. Confirm provider scrolling, long-press actions, half-width packing, and committed resize remain stable.
