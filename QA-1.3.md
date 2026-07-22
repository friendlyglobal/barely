# Barely 1.3 All apps acceptance record

This record captures the automated and emulator evidence for configurable All apps layouts and continuous Home return motion. Physical owner acceptance on the Galaxy S24 Ultra remains the final daily-driver confirmation after downloading the immutable GitHub asset.

## Identity

- Application ID: `app.usefriendly.barely`
- Version code: `17`
- Version name: `1.3.0`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)
- Compile Android: API 37.0

## Scope

- Command and Classic pass the same persisted layout preferences into one shared app collection.
- List remains the default; published icons remain off by default so existing users keep the 1.2 appearance after upgrade.
- Users can enable icons in list or grid, select three to six columns, and select four to eight visible rows. Reset returns to 4 × 6.
- App launch, long press, notification dots, Private Space, and search use the same callbacks in every layout.
- The All apps search control and shared Home command bar use one 20 dp bottom-spacing token.
- The icon preference also controls app and shortcut-owner icons in shared Home results and recent command history.
- The global search/command radius shapes the prompt, command results, history entries, and draggable history header.
- The All apps search surface uses a shape-aware Material 3 click target, a 24 dp leading icon, and 16 dp leading/gap geometry; its pressed state cannot draw a square outside the configured radius.
- The app count and header actions use the same foreground color hierarchy.
- Classic Home requests animate the pager from Apps or Favorites/Widgets to the center with the existing restrained spring.

## Automated gates

| Gate | Result |
| --- | --- |
| Settings migration tests | Pass locally for defaults, persisted grid choices, invalid layout fallback, and bounded density recovery. |
| Full unit suite | Pass in the final release verification. |
| Android lint | Pass with no blocking findings in the final release verification. |
| Debug APK | Built and installed on the Android emulator. |
| Optimized APK and AAB | Pass; both production-signed artifacts were rebuilt with R8/resource shrinking. |
| Signing identity | Pass; APK signer certificate SHA-256 is `afb18d2a200a0bd8273033cc1474862b868470b874a896a1e7d3d499c546dd48`. |
| Manifest privacy | Unchanged: no Internet permission, analytics, account system, database, or new runtime permission. |

## Emulator runtime evidence

- Captured the 1.2 text-only list before implementation and compared it beside the 1.3 list-with-icons and balanced 4 × 6 grid at the same 1344 × 2992 viewport.
- Confirmed the default text-only list remains unchanged until the user selects another option.
- Selected Grid and Show app icons in Settings, then verified all installed apps rendered in the shared collection.
- Moved the discrete sliders from 4 × 6 to 6 × 8 and confirmed both values and the denser grid persisted.
- Switched back to List with icons and confirmed icon, label, launch, and long-press targets remained aligned.
- Enabled icons, searched `cal` on shared Home, and confirmed the Calendar app and Clock shortcut-owner icons render in the ranked command results.
- Opened Calendar from that result, pulled up recent history, and confirmed the same preference renders its app icon beside the saved `cal → Calendar` entry.
- Confirmed the 12 dp global radius renders on ranked command surfaces and the draggable recent-history header from the same persisted preference.
- Compared the supplied Galaxy Apps screenshot beside the final 1.3 emulator state at matched dimensions: the search icon and placeholder now share the centered Material 3 row, the count uses the header foreground, and the 24 dp settings icon keeps its 48 dp target.
- Captured the All apps search control while pressed and confirmed the state layer remains clipped to the configured rounded shape with no square focus/ripple artifact.
- Captured intermediate frames from Apps → Home and Favorites/Widgets → Home, confirming the page position follows a continuous spring instead of teleporting to the center.
- Installed a production-signed 1.3 candidate over the signed 1.2 build and confirmed version code 17 opens without repeating onboarding, then reinstalled the final signed rebuild over the preserved 1.3 data for the final visual checks.

## Physical acceptance still required

On the Galaxy S24 Ultra, upgrade the production-signed 1.2 APK to 1.3 and verify:

1. Confirm All apps initially keeps the previous text-only list.
2. Enable app icons, switch between List and Grid, and try the 3 × 4, 4 × 6, and 6 × 8 density extremes.
3. Long-press WhatsApp in both layouts, scroll its shortcuts, and dismiss the sheet without a crash.
4. Press Home once from Apps and once from Favorites/Widgets; both pages should visibly slide to the center without overshoot or delay.
5. Restart Barely and confirm layout, icons, columns, rows, Home style, widgets, and favorites persist.
