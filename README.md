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
- Searches both apps and published App Shortcuts, such as “New incognito tab” when Chrome provides it.
- Combines apps and shortcuts into one relevance-ranked list without flooding app-name searches with unrelated conversations.
- Tolerates small typos, transposed characters, initials, word prefixes, case, and accents with an on-device fuzzy matcher.
- Provides a one-handed search entry point at the bottom of the all-apps page.
- Launches the best result when the keyboard Search action is pressed.
- Opens shortcuts with `LauncherApps.startShortcut`.
- Shows published shortcuts, favorite actions, app info, and uninstall from a long press.
- Refreshes apps and shortcuts through `LauncherApps.Callback`, including `onShortcutsChanged`.
- Persists favorites with `SharedPreferences`; no database is used.
- Supports work profiles and hidden profiles when Android exposes them to the launcher.
- Returns to the clean wallpaper page whenever the Home gesture or button is pressed.

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

- `MainActivity.kt` — small state holder, wallpaper window setup, and `ROLE_HOME` request.
- `LauncherUi.kt` — pager, favorites and app lists, search, and app action sheet.
- `LauncherRepository.kt` — `LauncherApps` calls, callbacks, profiles, shortcuts, and favorites.
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

## Test checklist

1. Press Home and confirm that only the wallpaper and system bars remain visible.
2. Swipe left for all apps and right for favorites.
3. Swipe up from the wallpaper to open search and the keyboard.
4. Tap an app name to launch it.
5. Long-press an app and verify its shortcuts, favorite action, app info, and uninstall action.
6. Add a favorite and confirm that it persists after restarting Launchly.
7. Search for a shortcut published by an installed app and confirm that a direct label match appears above apps.
8. Press the keyboard Search action and confirm that the top result opens directly.
9. Change the Android system language and reopen Launchly to verify automatic localization.

## Privacy

Launchly does not request internet access, collect analytics, or maintain a database. Favorites remain in local `SharedPreferences`. App and shortcut information comes directly from Android’s `LauncherApps` service.

## Prototype limitations

- No folders, widgets, cloud backup, or drag-to-reorder support.
- Shortcut names and availability are controlled by the apps that publish them.
- Device manufacturers and work policies may hide apps or profiles from launcher APIs.
- Release `0.2` is debug-signed and intended for development testing.

## Official Android references

- [Material 3 for Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [RoleManager](https://developer.android.com/reference/android/app/role/RoleManager)
- [LauncherApps](https://developer.android.com/reference/android/content/pm/LauncherApps)
- [LauncherApps.ShortcutQuery](https://developer.android.com/reference/android/content/pm/LauncherApps.ShortcutQuery)
