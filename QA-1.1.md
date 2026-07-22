# Barely 1.1 widget acceptance record

This record captures the automated and emulator evidence for the one-page widget refinement. Physical owner acceptance on the Galaxy S24 Ultra remains the final daily-driver confirmation after downloading the immutable GitHub asset.

## Identity

- Application ID: `app.usefriendly.barely`
- Version code: `15`
- Version name: `1.1.0`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)
- Compile Android: API 37.0
- Release mode: R8 optimized, resource-shrunk, and production-signed only when all environment secrets are present

## Scope

- Favorites and widgets remain one page left of Home; no new top-level or paginated widget surface exists.
- The Compose frame previews width and height continuously, while `AppWidgetHostView.updateAppWidgetSize` receives only committed dimensions during a drag.
- Scrollable descendants inside hosted Android widgets get first refusal on touch and release it at their own edge.
- Press-and-hold opens a contextual action menu below the widget without launching its provider content. Reconfiguration is shown only for Android 12+ providers that publish `WIDGET_FEATURE_RECONFIGURABLE`.
- Resize handles have accessible touch bounds and narrow system-gesture exclusion regions.
- The existing four-span packing and foldable two-pane behavior remain intact.

## Automated gates

| Gate | Result |
| --- | --- |
| Unit suite | Pass locally, including provider-size commit policy and deterministic side-by-side row packing. |
| Android lint | Pass locally with no errors. |
| Debug APK | Built and installed on the Android emulator. |
| Optimized APK and AAB | Pass: `scripts/verify-release.sh` completed with R8, resource shrinking, certificate verification, and checksums. |
| Manifest privacy | Unchanged: no Internet permission, analytics, account system, or new runtime permission. |

## Emulator runtime evidence

- Bound real Google Calendar month and schedule widgets through Barely's existing preview picker.
- Resized a Calendar widget horizontally and vertically with the frame following the complete drag.
- Observed one `AppWidgetServiceImpl.updateAppWidgetOptions` call after each completed gesture and none per drag frame.
- Started a right-edge resize inside the exclusion target without Android's back gesture closing Barely.
- Opened the real Calendar widget's long-press menu, entered Resize and arrange from it, dismissed it outside, and opened Calendar's correct Android App info page.
- Confirmed the hosted `AppWidgetHostView` exposes Android's long-clickable accessibility semantics.
- Restarted and upgraded the debug build in place; widget IDs and saved layout metadata remained attached.
- Installed the production-signed 1.0.0 APK (version code 14), upgraded it in place to the production-signed 1.1.0 candidate (version code 15), and launched the upgraded activity successfully.
- Verified the compact one-page Favorites/Widgets layout and real widget rendering after the update.

## Physical acceptance still required

On the Galaxy S24 Ultra, install the production-signed 1.1 APK over 1.0, keep Barely selected as Home, and verify:

1. Add Samsung Calendar, Reminder, or another vertically scrollable widget.
2. Scroll inside the widget, then continue at its top and bottom edges to verify the outer page takes over naturally.
3. Resize from the right and bottom handles slowly and quickly; the provider content must not flash or disappear during the drag.
4. Resize two compatible widgets to half width and confirm they share one row after leaving edit mode.
5. Rotate once, restart Barely, and confirm widget order, size, alignment, and interaction persist.
6. Enable TalkBack and use the localized increase/decrease size actions without relying on edge dragging.
7. Long-press each provider once. Confirm the menu appears without opening the widget, App info targets the provider, Remove is reachable, and Widget settings appears only on providers that support reconfiguration.
