# Barely

Barely is a minimal, wallpaper-first Android launcher built with Kotlin and Jetpack Compose. It uses Android launcher APIs directly and intentionally avoids databases, dependency injection, and unnecessary architecture.

## Download

Download the latest APK from [GitHub Releases](https://github.com/friendlyglobal/barely/releases/latest).

The current release is a debug-signed prototype intended for testing. Android may display an additional warning when installing it outside Google Play.

## Features

- Requests the default home role through `RoleManager.ROLE_HOME`.
- Lists launchable activities from every accessible profile with `LauncherApps.getActivityList`.
- Opens apps with `LauncherApps.startMainActivity`.
- Keeps the system wallpaper visible on every page, including live wallpapers, through `FLAG_SHOW_WALLPAPER`.
- Uses a three-page horizontal layout: favorites on the left, an empty wallpaper home page in the center, and all apps on the right.
- Offers an optional Command Home during onboarding and in settings for people who prefer opening apps deliberately by typing.
- Keeps Command search local, reuses the same fuzzy app/shortcut/command ranking, shows no more than three suggestions, and supports touch, keyboard, mouse, and DeX.
- Provides explicit `:apps`, `:settings`, and `:classic` exits so Command mode never traps the user.
- Lets Command users choose a background tint and opacity from solid color to a completely transparent wallpaper, tune the shared prompt/top-action corner radius from 0–32 dp, and optionally enable Terminal aesthetics; the preferences never leave the device.
- Opens full-screen search with an upward swipe from the center page.
- Dismisses search by pulling down from its top handle directly to the wallpaper Home page, pressing Back, or tapping the back arrow.
- Searches both apps and published App Shortcuts, such as “New incognito tab” when Chrome provides it.
- Combines apps and shortcuts into one relevance-ranked list without flooding app-name searches with unrelated conversations.
- Tolerates small typos, transposed characters, initials, word prefixes, case, and accents with an on-device fuzzy matcher.
- Provides a one-handed search entry point at the bottom of the all-apps page.
- Keeps the search field capsule-shaped and builds its optional recommendations, recent searches, and dismissible command tip upward from the keyboard.
- Renders Classic Search results from the bottom up so the best match stays directly above the capsule and within one-handed reach.
- Learns recommendations from app launches and successful query-to-app/shortcut choices made through Barely, with local recency decay, no Usage Access permission, and controls to disable or clear the complete history.
- Uses short, restrained transitions for page changes and search instead of moving the whole screen as one sheet.
- Launches that bottom, best-ranked result when the phone keyboard's Search action is pressed.
- Opens shortcuts with `LauncherApps.startShortcut`.
- Lets published App Shortcuts be pinned to Favorites and opened directly without first opening their app.
- Shows published shortcuts, favorite actions, app info, and uninstall from a long press.
- Refreshes apps and shortcuts through `LauncherApps.Callback`, including `onShortcutsChanged`.
- Persists favorites with `SharedPreferences`; no database is used.
- Hosts user-selected Android widgets in a vertical glanceable stack below favorites.
- Provides a searchable widget catalog grouped by app with published previews, grid dimensions, and icon fallbacks, then uses each provider's standard binding and configuration flow; Barely persists only widget IDs and simple layout metadata, never widget contents.
- Lets users resize hosted widgets live by dragging their right or bottom edge, then align and reorder them from a separate edit toolbar. TalkBack exposes localized increase/decrease width and height actions, and all edit controls retain 48 dp touch targets; layout metadata stays in local `SharedPreferences` alongside the widget IDs.
- Places compatible half-width widgets side by side in a simple four-column grid after edit mode closes.
- Supports work profiles and hidden profiles when Android exposes them to the launcher.
- Returns to the clean wallpaper page whenever the Home gesture or button is pressed.
- Locks the screen on a home-page double tap and opens notifications on a downward swipe through an optional, narrowly configured Accessibility service.
- Adapts at runtime to phones, split-screen windows, tablets, and foldables: one column on compact windows, two on medium/foldable windows, and three only on expanded windows.
- Uses Jetpack WindowManager `FoldingFeature` data to keep controls and list items away from a separating vertical fold or hinge.
- Uses Android 12+ cross-window blur on Favorites, Apps, and Search for a native frosted-wallpaper effect; a translucent gradient automatically takes over when the system disables blur.
- Keeps the center Home page completely transparent and free of launcher chrome.
- Separates Android 15 Private Space apps, locks or unlocks the profile through Android, and removes locked private content from launcher search.
- Includes a local command palette for arithmetic, unit conversion, and quick settings without network access.
- Can optionally search contacts in memory after an explicit runtime permission request.
- Keeps AI out of local results while an app, shortcut, contact, calculation, or command matches; only then offers the preferred installed ChatGPT, Gemini, or Claude app through Android sharing. The assistant is selected during onboarding or in Settings, and Barely contains no AI API key or relay server.
- Supports keyboard and mouse use on DeX, including page arrows, Ctrl+K, result selection, Enter, Escape, and right-click app actions.
- Offers disabled-by-default notification dots and media controls through Android's notification-listener access screen.
- Opens launcher settings as a full-screen scrollable page and keeps app actions reachable even when an app publishes a long shortcut list.
- Lets double tap and swipe down be mapped to a small public-Android action set, including no action, lock/notifications, Search, and All apps where the current Home style supports them.
- Exports and imports portable JSON preferences through Android's document picker while excluding widget IDs, permission grants, profile state, favorites, and local history.

Shortcut data from other apps is protected by Android. It becomes available only after Barely is the default launcher and `LauncherApps.hasShortcutHostPermission()` returns `true`.

## Languages

Barely automatically follows the Android system language. English is the fallback language, with localized resources for:

- Arabic
- Chinese, Simplified
- French
- German
- Hindi
- Indonesian
- Italian
- Japanese
- Korean
- Portuguese, Brazil
- Russian
- Spanish

Android selects the matching resource automatically. On Android 13 and newer, Barely also exposes these languages in the system’s per-app language settings through an auto-generated `LocaleConfig`. Arabic uses Android’s right-to-left layout support.

## Roadmap

See [ROADMAP.md](ROADMAP.md) for the intentionally scoped plan from 0.5 through 1.0. The roadmap keeps Home wallpaper-first, makes sensitive modules optional, and rejects features that require a Barely backend or private OEM APIs.

Version 0.5 adopts the permanent `app.usefriendly.barely` application ID. Android treats it as a different app from the Launchly 0.4 prototype, so testers must select Barely as Home again and re-add widgets after installing it.

## Project structure

- `MainActivity.kt` — small state holder, wallpaper window setup, `ROLE_HOME`, and widget picker results.
- `LauncherUi.kt` — pager, favorites and app lists, search, and app action sheet.
- `LauncherRepository.kt` — `LauncherApps` calls, callbacks, profiles, shortcuts, and favorites.
- `WidgetHostController.kt` — the platform `AppWidgetHost` lifecycle and locally persisted widget IDs.
- `LauncherAccessibilityService.kt` — optional global lock/notification actions; event collection and screen-content access are disabled.
- `CommandPalette.kt` — local calculator, conversions, contacts, quick settings, and explicit AI-app handoff.
- `LauncherNotificationService.kt` — optional in-memory notification counts and active media-session controls.
- `LauncherModels.kt` — lightweight app and shortcut models.
- `LauncherTheme.kt` — the stable neutral Material 3 palette, shape scale, readable widths, and wallpaper scrim tokens.
- `res/values-*/strings.xml` — system-locale translations.

## Requirements

- JDK 17
- Android SDK Platform 37.0
- Android Gradle Plugin 9.1.1
- Gradle 9.3.1
- Android 10 or newer (`minSdk 29`)

The project uses AGP built-in Kotlin, Compose Compiler 2.3.21, and Material 3 `1.5.0-alpha24`. The Material version is pinned because the current clickable and long-clickable List Item APIs are still pre-release.

## Build

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew lintDebug assembleDebug
```

The APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

If Gradle reports duplicated generated names such as `BuildConfig 2.java` or `... 4.class`, the project directory is being synchronized while Gradle writes intermediate files. Move the project outside iCloud, Google Drive, or another live-sync folder, run `./gradlew clean`, and build again.

## Install on a Galaxy S24 Ultra

### Android Studio

1. Open the project in Android Studio Panda 3 or newer.
2. Under **Settings > Build, Execution, Deployment > Build Tools > Gradle**, select JDK 17 or the compatible embedded JDK.
3. Under **Tools > SDK Manager**, install Android SDK Platform 37.0 and Android SDK Platform-Tools.
4. On the phone, open **Settings > About phone > Software information** and tap **Build number** seven times.
5. Return to **Developer options**, enable **USB debugging**, connect the phone, and accept the RSA prompt.
6. Select the Galaxy S24 Ultra in Android Studio and run the `app` configuration.

### ADB

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Set as the default launcher

1. Open Barely and swipe left to the **Apps** page.
2. Tap **Set** in the default home card and approve the Android role dialog.
3. On Samsung devices, the same setting is available under **Settings > Apps > Choose default apps > Home app**.
4. Select **Barely**.

To return to Samsung’s launcher, select **One UI Home** from the same system page before uninstalling Barely.

## Configure optional Home gestures

The clean home page works without Accessibility. This one optional service is needed only because Android does not let an ordinary third-party launcher lock the screen or open notifications directly.

1. In **Barely settings > Gestures**, choose the action for double tap and swipe down. Lock screen and Notifications require the optional service; Search, All apps, and Nothing do not.
2. Android opens the **Barely gestures** service page directly when the device permits it. On Samsung it falls back to **Accessibility > Installed apps**; other devices open Accessibility with **Barely gestures** highlighted. Tap **Barely gestures** and enable the service if one final tap is required.
3. If Samsung blocks the switch for a sideloaded APK, open **Settings > Apps > Barely**, use the three-dot menu, choose **Allow restricted settings**, then return to Accessibility.

The service declares `canRetrieveWindowContent=false` and disables accessibility event collection when connected. It performs only Android’s global **Lock screen** and **Notifications** actions. Disabling the service removes both gestures without affecting the launcher.

## Test checklist

1. Press Home and confirm that only the wallpaper and system bars remain visible.
2. Swipe left for all apps and right for favorites.
3. Swipe up from the wallpaper to open search and the keyboard.
4. Pull down from the handle at the top of search and confirm that the content follows the gesture and closes.
5. Tap an app name to launch it.
6. Long-press an app and verify its shortcuts, favorite action, app info, and uninstall action.
7. Add a favorite and confirm that it persists after restarting Barely.
8. On the Favorites page, tap the plus button beside **Widgets**, choose a provider, and finish its configuration when requested.
9. Interact with the hosted widget, restart Barely to verify persistence, then remove it with its close button.
10. Search for a shortcut published by an installed app and confirm that a direct label match appears above apps.
11. Confirm that Classic Search grows upward with the best result nearest the bottom capsule, then press the phone keyboard's Search action and verify that this result opens directly.
12. Change the Android system language and reopen Barely to verify automatic localization.
13. In Settings, map double tap and swipe down to each available action. Verify Lock screen and Notifications request gesture access, while Search, All apps, and Nothing do not.
14. On a foldable emulator or unfolded device, verify the two-pane Favorites/Widgets page and the appropriate two- or three-column Apps/search layout. Resize the window and confirm it returns to one column below 600 dp.
15. On Android 15 or newer, configure Private Space, verify its separate Apps container, lock it, and confirm its apps and shortcuts disappear from search.
16. Search for `18*12`, `10 km to mi`, or `wifi`; verify calculations and conversions stay local and settings open directly.
17. With a physical keyboard, use Left/Right, Ctrl+K, Up/Down, Enter, and Escape. Right-click an app with a mouse to open its actions.
18. Search for `notifications`, read the warning, and enable access only if desired. Verify dots and media controls can each remain independently disabled.
19. Move between Home, Favorites, Apps, and Search. Confirm Home remains clear and other pages use wallpaper blur or the translucent fallback.
20. Select **Command** in Barely settings, type an app and a published shortcut, and verify that at most three local results appear. Test `:apps`, `:settings`, and `:classic`.
21. Change the Command background color, opacity, and search/command radius. Verify that the prompt, All apps search entry, full Search input, and optional top-action capsule update together. Enable Terminal aesthetics and confirm the `>` prompts, arrows, and monospace type return; disable it and confirm the clean style. Test the local resets, then use the section Reset and confirm black, 42%, 12 dp, top contrast off, and Terminal aesthetics off are restored.
22. Drag up in Command and verify that the history page follows the finger continuously before snapping open or closed. Confirm that successful app/shortcut searches appear as a command-line-style history log, open an entry, and verify that it moves to the most recent position. Use `:clearhistory` to remove it.
23. Enter widget edit mode, drag the right and bottom handles, and confirm that the preview follows the gesture, the size persists, and the page does not swipe away.
24. Resize two compatible widgets to half width, leave edit mode, and confirm that they share one row without overlapping.
25. Enable TalkBack and verify that top commands, settings choices, widget editing controls, and resize actions receive a clear focus target and label.
26. Set the system font to 150% and use an RTL language. Enable Terminal aesthetics and confirm that localized content follows RTL while Command syntax still reads `:command`, `>_`, and `>`.
27. Disable Android animator scales and repeat Home, Apps, Search, and back navigation; every state change must remain usable without relying on motion.
28. With ChatGPT, Gemini, or Claude installed, select a preferred assistant. Type a query with no local match and verify exactly the selected assistant action appears; type an app or shortcut match and verify the AI action stays hidden.
29. Pin a published shortcut from an app action sheet, open it from Favorites, unpin it, and verify unavailable shortcuts disappear after the publisher removes them.
30. Export settings, change appearance and gesture choices, import the file, and verify the portable preferences return without altering widgets, permissions, favorites, or local search history.

## Privacy

Barely does not request internet access, collect analytics, maintain a database, or allow Android cloud backup. Favorites, selected widget IDs, Command appearance, successful local app/shortcut query associations, and opt-in feature switches remain in local `SharedPreferences`. App, shortcut, and profile information comes directly from Android’s `LauncherApps` service, while widget contents are rendered and updated by their provider apps through Android's `AppWidgetHost` APIs. Disabling local suggestions stops new ranking/history writes and hides the saved suggestions; clearing local history removes them. The optional Accessibility service cannot retrieve window content and subscribes to no accessibility events after connecting.

Contact search is off until its runtime permission is granted. Matching contact names and phone numbers stay in process memory and Barely saves none of them. AI handoff uses an explicit Android `ACTION_SEND` intent to an installed assistant; Barely never sees the assistant's response.

Notification and media integration is also off by default and requires approval on Android's dedicated notification-access screen. That system permission is sensitive because a notification listener can access notification metadata while enabled. Barely derives per-app counts and current media metadata in memory, persists no notification content, and exposes independent switches for dots and media controls. Revoking notification access immediately disables both data sources.

## Prototype limitations

- No folders, cloud backup, or drag-and-drop favorite organization yet; widget ordering currently uses deliberate edit controls.
- Shortcut names and availability are controlled by the apps that publish them.
- Device manufacturers and work policies may hide apps or profiles from launcher APIs.
- Cross-window blur requires Android 12 or newer and can be disabled by the device at runtime; the UI retains a contrast-safe fallback.
- Development builds are debug-signed and intended for testing until the production signing flow is finalized.

## Official Android references

- [Material 3 for Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [RoleManager](https://developer.android.com/reference/android/app/role/RoleManager)
- [LauncherApps](https://developer.android.com/reference/android/content/pm/LauncherApps)
- [LauncherApps.ShortcutQuery](https://developer.android.com/reference/android/content/pm/LauncherApps.ShortcutQuery)
- [Build a widget host](https://developer.android.com/develop/ui/views/appwidgets/host)
- [Build adaptive apps](https://developer.android.com/develop/ui/compose/build-adaptive-apps)
- [Make an app fold aware](https://developer.android.com/develop/adaptive-apps/guides/foldables/make-your-app-fold-aware)
- [AccessibilityService global actions](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#performGlobalAction(int))
- [Android 15 Private Space launcher requirements](https://developer.android.com/about/versions/15/behavior-changes-all#private-space)
- [Window blurs](https://source.android.com/docs/core/display/window-blurs)
- [NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService)
- [MediaSessionManager](https://developer.android.com/reference/android/media/session/MediaSessionManager)
