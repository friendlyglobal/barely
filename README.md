# Launchly

Launchly is a minimal, wallpaper-first Android launcher built with Kotlin and Jetpack Compose. It uses Android launcher APIs directly and intentionally avoids databases, dependency injection, and unnecessary architecture.

## Download

Download the latest APK from [GitHub Releases](https://github.com/friendlyglobal/launchly/releases/latest).

The current release is a debug-signed prototype intended for testing. Android may display an additional warning when installing it outside Google Play.

## Features

- Requests the default home role through `RoleManager.ROLE_HOME`.
- Lists launchable activities from every accessible profile with `LauncherApps.getActivityList`.
- Opens apps with `LauncherApps.startMainActivity`.
- Keeps the system wallpaper visible on every page, including live wallpapers, through `FLAG_SHOW_WALLPAPER`.
- Uses a three-page horizontal layout: favorites on the left, an empty wallpaper home page in the center, and all apps on the right.
- Opens full-screen search with an upward swipe from the center page.
- Dismisses search by pulling down from its top handle, pressing Back, or tapping the back arrow.
- Searches both apps and published App Shortcuts, such as “New incognito tab” when Chrome provides it.
- Combines apps and shortcuts into one relevance-ranked list without flooding app-name searches with unrelated conversations.
- Tolerates small typos, transposed characters, initials, word prefixes, case, and accents with an on-device fuzzy matcher.
- Provides a one-handed search entry point at the bottom of the all-apps page.
- Uses short, restrained transitions for page changes and search instead of moving the whole screen as one sheet.
- Launches the best result when the keyboard Search action is pressed.
- Opens shortcuts with `LauncherApps.startShortcut`.
- Shows published shortcuts, favorite actions, app info, and uninstall from a long press.
- Refreshes apps and shortcuts through `LauncherApps.Callback`, including `onShortcutsChanged`.
- Persists favorites with `SharedPreferences`; no database is used.
- Hosts user-selected Android widgets in a vertical glanceable stack below favorites.
- Uses Android's widget picker and each provider's configuration screen; widget IDs are the only widget data Launchly persists.
- Supports work profiles and hidden profiles when Android exposes them to the launcher.
- Returns to the clean wallpaper page whenever the Home gesture or button is pressed.
- Locks the screen on a home-page double tap and opens notifications on a downward swipe through an optional, narrowly configured Accessibility service.
- Adapts at runtime to phones, split-screen windows, tablets, and foldables: one column on compact windows, two on medium/foldable windows, and three only on expanded windows.
- Uses Jetpack WindowManager `FoldingFeature` data to keep controls and list items away from a separating vertical fold or hinge.

Shortcut data from other apps is protected by Android. It becomes available only after Launchly is the default launcher and `LauncherApps.hasShortcutHostPermission()` returns `true`.

## Languages

Launchly automatically follows the Android system language. English is the fallback language, with localized resources for:

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

Android selects the matching resource automatically. On Android 13 and newer, Launchly also exposes these languages in the system’s per-app language settings through an auto-generated `LocaleConfig`. Arabic uses Android’s right-to-left layout support.

## Project structure

- `MainActivity.kt` — small state holder, wallpaper window setup, `ROLE_HOME`, and widget picker results.
- `LauncherUi.kt` — pager, favorites and app lists, search, and app action sheet.
- `LauncherRepository.kt` — `LauncherApps` calls, callbacks, profiles, shortcuts, and favorites.
- `WidgetHostController.kt` — the platform `AppWidgetHost` lifecycle and locally persisted widget IDs.
- `LauncherAccessibilityService.kt` — optional global lock/notification actions; event collection and screen-content access are disabled.
- `LauncherModels.kt` — lightweight app and shortcut models.
- `LauncherTheme.kt` — Material 3, dynamic colors, and light/dark fallbacks.
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

1. Open Launchly and swipe left to the **Apps** page.
2. Tap **Set** in the default home card and approve the Android role dialog.
3. On Samsung devices, the same setting is available under **Settings > Apps > Choose default apps > Home app**.
4. Select **Launchly**.

To return to Samsung’s launcher, select **One UI Home** from the same system page before uninstalling Launchly.

## Enable the optional home gestures

The clean home page works without Accessibility. This one optional service is needed only because Android does not let an ordinary third-party launcher lock the screen or open notifications directly.

1. On Launchly’s wallpaper page, double-tap or swipe down once.
2. Android opens **Settings > Accessibility**. Select **Launchly gestures** and enable it.
3. If Samsung blocks the switch for a sideloaded APK, open **Settings > Apps > Launchly**, use the three-dot menu, choose **Allow restricted settings**, then return to Accessibility.

The service declares `canRetrieveWindowContent=false` and disables accessibility event collection when connected. It performs only Android’s global **Lock screen** and **Notifications** actions. Disabling the service removes both gestures without affecting the launcher.

## Test checklist

1. Press Home and confirm that only the wallpaper and system bars remain visible.
2. Swipe left for all apps and right for favorites.
3. Swipe up from the wallpaper to open search and the keyboard.
4. Pull down from the handle at the top of search and confirm that the content follows the gesture and closes.
5. Tap an app name to launch it.
6. Long-press an app and verify its shortcuts, favorite action, app info, and uninstall action.
7. Add a favorite and confirm that it persists after restarting Launchly.
8. On the Favorites page, tap the plus button beside **Widgets**, choose a provider, and finish its configuration when requested.
9. Interact with the hosted widget, restart Launchly to verify persistence, then remove it with its close button.
10. Search for a shortcut published by an installed app and confirm that a direct label match appears above apps.
11. Press the keyboard Search action and confirm that the top result opens directly.
12. Change the Android system language and reopen Launchly to verify automatic localization.
13. On the wallpaper page, swipe down and verify that notifications open; double-tap and verify that the phone locks.
14. On a foldable emulator or unfolded device, verify the two-pane Favorites/Widgets page and two-column Apps/search layouts. Resize the window and confirm it returns to one column below 600 dp.

## Privacy

Launchly does not request internet access, collect analytics, maintain a database, or allow Android cloud backup. Favorites and selected widget IDs remain in local `SharedPreferences`. App and shortcut information comes directly from Android’s `LauncherApps` service, while widget contents are rendered and updated by their provider apps through Android's `AppWidgetHost` APIs. The optional Accessibility service cannot retrieve window content and subscribes to no accessibility events after connecting.

## Prototype limitations

- No folders, cloud backup, widget resizing, or drag-to-reorder support.
- Shortcut names and availability are controlled by the apps that publish them.
- Device manufacturers and work policies may hide apps or profiles from launcher APIs.
- Release `0.3` is debug-signed and intended for development testing.

## Official Android references

- [Material 3 for Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [RoleManager](https://developer.android.com/reference/android/app/role/RoleManager)
- [LauncherApps](https://developer.android.com/reference/android/content/pm/LauncherApps)
- [LauncherApps.ShortcutQuery](https://developer.android.com/reference/android/content/pm/LauncherApps.ShortcutQuery)
- [Build a widget host](https://developer.android.com/develop/ui/views/appwidgets/host)
- [Build adaptive apps](https://developer.android.com/develop/ui/compose/build-adaptive-apps)
- [Make an app fold aware](https://developer.android.com/develop/adaptive-apps/guides/foldables/make-your-app-fold-aware)
- [AccessibilityService global actions](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#performGlobalAction(int))
