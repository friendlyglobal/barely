# Barely 1.6.1 icon-rendering acceptance record

This patch corrects custom icon masks without adding manufacturer allowlists. Android apps can mix adaptive and legacy icons on the same Samsung, Google, or Xiaomi device, so Barely selects the rendering path from each icon's published capability.

## Identity

- Application ID: `app.usefriendly.barely`
- Version code: `21`
- Version name: `1.6.1`
- Minimum Android: 10 (API 29)
- Target Android: 16 (API 36)

## Rendering contract

- Original uses Android's profile-badged launcher icon unchanged.
- Circle, Squircle, and Rounded square rebuild `AdaptiveIconDrawable` foreground and background layers and apply exactly one selected mask.
- Legacy bitmaps whose content reaches the corners are inset inside a same-edge-color mask so artwork is not cut off.
- Transparent legacy artwork keeps its complete published silhouette; Barely does not invent an opaque tile behind it.
- Work-profile badges are applied only after the custom icon is rendered.
- Changing shape refreshes the in-memory app snapshot immediately and persists across relaunch.

## Emulator acceptance

1. Enable Grid and Show app icons, then compare Original, Circle, Squircle, and Rounded square at the same viewport.
2. Inspect adaptive Calendar, Camera, Chrome, Google, YouTube, and YouTube Music icons. Their foreground artwork must remain complete while the outer silhouette follows the selected mask.
3. Inspect a transparent legacy icon such as the emulator's T-Mobile SIM Toolkit. Its irregular artwork must remain complete and must not gain a fabricated background tile.
4. Return to Home, Search, history, Favorites, and app actions. The same pre-rendered bitmap must appear without a second Compose clip.
5. Force-stop and reopen Barely. The selected shape and rendered collection must persist.
6. With Android Private Space empty, confirm the expanded container explains that no apps are available. When it is locked, tapping anywhere on its header must request unlock rather than only changing the disclosure arrow.

## Galaxy S24 Ultra acceptance

1. Upgrade the production-signed 1.6 APK to 1.6.1 without uninstalling and confirm Home role, favorites, widgets, and launcher settings remain intact.
2. Compare WhatsApp, WA Business, Samsung Wallet, Vivo, Xiaomi Home, YouTube, and other mixed OEM icons under all four choices.
3. Verify each adaptive icon changes outer shape without losing its foreground, and each legacy icon keeps the full artwork with safe spacing when needed.
4. Search for the same apps and open an app action sheet; the drawer, command results, history, favorites, and sheet must show the same rendering.
5. Switch back to Original and verify Android's published One UI appearance returns unchanged.
6. Confirm an empty Android Private Space is explicitly labeled. Do not expect Barely to expose Samsung Secure Folder apps; Secure Folder is a separate Knox-managed surface.

The emulator validates the rendering categories and state changes. Only the physical S24 Ultra can close the One UI acceptance gate for the exact installed icon set.
