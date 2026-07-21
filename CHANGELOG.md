# Changelog

All notable changes to Barely are documented in this file.

## 0.7 — Unreleased

### Added

- Added a swipe-up Terminal history drawer with tappable recent app results, pull-down dismissal from the list edge, and local query-to-app learning.
- Added `:history` and `:clearhistory` Terminal commands backed by the same local history controls as Settings.
- Added a shared 0–32 dp Terminal corner-radius control for the prompt and optional top-action capsule, with a squarer 12 dp default plus local radius/opacity resets and a complete appearance reset.
- Added localized TalkBack actions for increasing or decreasing widget width and height without relying on edge-drag gestures.

### Changed

- Replaced Android wallpaper-derived Dynamic Color with a stable neutral Barely palette and consistent shape scale.
- Moved Terminal Apps and Settings actions to floating top-right commands, with an optional contrast capsule for busy wallpapers.
- Refined app actions into compact grouped rows with a calmer, scrollable shortcuts section.
- Strengthened wallpaper-aware reading scrims without replacing the wallpaper, and constrained Search/Terminal controls to a comfortable width on foldables and DeX.
- Made Search fully fade the previous Classic page during entry so app names never compete with results or the bottom input.
- Consolidated spacing, shape, opacity, elevation, blur, and motion values into a shared Barely token system, and restored 48 dp touch targets on compact controls.

### Privacy

- Search learning stores only successful app or published-shortcut queries and identifiers locally so the chosen result can be reopened. Contacts, commands, and AI questions are never learned, and Settings or `:clearhistory` removes the complete local log.

### Fixed

- Kept the Terminal prompt within the 600 dp readable-width limit on unfolded and DeX windows.
- Prevented Terminal Home commands and its prompt from remaining interactive or visually leaking beneath the expanded All apps surface.
- Preserved literal `:command`, `>_`, and `>` syntax in RTL locales while leaving localized content in the system text direction.

## 0.6 — 2026-07-21

### Added

- Added an optional Terminal Home that can be selected during onboarding or changed later in Barely settings.
- Added a wallpaper-first terminal prompt with the existing local fuzzy ranking for apps, App Shortcuts, and commands, capped at three actionable suggestions.
- Added `:apps`, `:settings`, and `:classic` escape commands plus touch, keyboard, mouse, and DeX navigation.
- Added a local Terminal background palette with adjustable opacity from solid color to fully transparent wallpaper.

### Changed

- Rebuilt widget editing around live right-edge width and bottom-edge height handles; widget previews now resize during the gesture and persist on release.
- Moved widget ordering, alignment, dimensions, and removal into a separate toolbar below the widget instead of covering provider content.
- Packed compatible half-width widgets side by side in a predictable four-column Favorites grid while keeping edit mode spacious and touch-friendly.
- Made Classic Search results grow upward from the bottom capsule, with the best-ranked result closest to the thumb and opened by the keyboard Search action.
- Smoothed Home, Search, and Terminal Apps transitions while keeping the wallpaper visible beneath each entering surface.
- Reduced wallpaper scrims and made the native blur flag conditional on Android actually enabling cross-window blur.

### Fixed

- Prevented horizontal page navigation from stealing resize gestures while widget edit mode is active.
- Removed the opaque gray fallback seen when cross-window blur is unavailable or disabled.
- Updated widget guidance and all thirteen locales for edge-based resizing and Terminal appearance controls.

## 0.5.1 — 2026-07-21

### Added

- Added a dedicated widget editing mode with persistent width, height, horizontal position, and vertical ordering.
- Added drag handles plus accessible move controls for resizing and reordering widgets.

### Fixed

- Made the bottom search entry on the Apps page a true capsule.
- Prevented widgets from being forced into one full-width, provider-minimum-height layout.
- Kept existing widget IDs compatible while migrating them to the new local layout metadata.

## 0.5 — 2026-07-21

### Added

- Established the local-first roadmap through 1.0.
- Renamed Launchly to Barely and adopted `app.usefriendly.barely` as the permanent application ID and Kotlin namespace.
- Added a launcher settings entry point beside the app count for gestures, appearance, optional modules, and system access.
- Made double-tap lock, swipe-down notifications, frosted wallpaper, notification dots, and media controls independently configurable.
- Added a capsule-shaped bottom search surface, a dismissible command hint, and a pull-down transition that returns directly to the wallpaper Home page.
- Added private, local-only app recommendations and successful recent app searches without requesting Usage Access or persisting shortcut/contact queries.
- Replaced the platform's flat widget list with a searchable catalog grouped by app, including provider previews, grid dimensions, and graceful icon fallbacks.
- Localized every new 0.5 settings, search, and widget-catalog surface across all thirteen supported system languages.
- Promoted launcher settings from a partial bottom sheet to a full-screen, independently scrollable page.
- Made app actions fully expanded and internally scrollable, with Favorite, App info, and Uninstall kept before long shortcut lists.

## 0.4 — 2026-07-21

### Added

- Added an Android 15 Private Space container with lock, unlock, collapse, profile callbacks, and correct exclusion of locked private apps and shortcuts from search.
- Added a fully local command palette for calculations, common unit conversions, and Android quick settings.
- Added optional in-memory contact search, requested only when the user explicitly enables it.
- Added explicit handoff of questions to installed ChatGPT, Gemini, or Claude apps without an API key, Barely server, or internet permission.
- Added full keyboard navigation for DeX, tablets, foldables, and external keyboards, including page navigation, search selection, Enter, Escape, and Ctrl+K.
- Added right-click app actions for mouse and trackpad use.
- Added optional notification dots and active-media controls backed by a disabled-by-default `NotificationListenerService`.
- Added native cross-window wallpaper blur on Android 12 and newer for Favorites, Apps, and Search.

### Changed

- Replaced flat gray page overlays with a restrained translucent gradient and a blur-aware fallback for devices, battery modes, or accessibility configurations that disable window blur.
- Kept the center Home page completely clear while making content pages easier to read over detailed wallpapers.
- Repositioned and simplified search so its input sits directly above the keyboard without a heavy outline.

## 0.3 — 2026-07-21

### Added

- Added a vertical Android widget stack below favorite apps.
- Added the system widget picker, provider configuration flow, local widget ID persistence, and removal.
- Added pull-down-to-dismiss from the top of search with direct gesture feedback.
- Added optional double-tap-to-lock and swipe-down-for-notifications home gestures without screen-content access or accessibility event collection.
- Localized the widget interface in all thirteen supported languages.
- Added responsive one-, two-, and three-column layouts plus hinge-aware foldable support through Jetpack WindowManager.

### Changed

- Reworked search entry into a short layered transition with a bottom-originating input bar.
- Tuned horizontal page snapping and added subtle depth feedback while paging.
- Made the empty favorites state compact so widgets remain immediately reachable.
- Prevented fuzzy matching against opaque package and shortcut IDs to remove unrelated search results.
- Moved the all-apps search entry into a true bottom section so app names never render behind it.
- Disabled Android cloud backup so favorites, widget IDs, and launcher preferences stay on the device.

## 0.2 — 2026-07-21

### Changed

- Removed package identifiers from app search results.
- Replaced separate app and shortcut sections with one compact relevance-ranked result list.
- App-name searches no longer flood the results with every shortcut published by that app.
- Uses each publisher's shortcut rank as a stable tie-breaker.

### Added

- Pressing the keyboard Search action now launches the best result immediately.
- Added lightweight fuzzy matching for typos, transposed characters, initials, word prefixes, and accent-insensitive text.
- Added a bottom search entry point to the all-apps page.
- Shortcut IDs participate as a low-priority fallback, improving discovery of conversation shortcuts that use a contact or thread identifier.
- Unit coverage for exact, prefix, fuzzy, initials, and accent-insensitive search ranking.

## 0.1 — 2026-07-21

Initial public prototype.

### Added

- Wallpaper-first three-page launcher interface.
- Default launcher role request with `ROLE_HOME`.
- App discovery and launch through `LauncherApps`.
- Favorites persisted locally without a database.
- App and shortcut search.
- Published App Shortcuts with direct `startShortcut` support.
- Long-press actions for shortcuts, favorites, app info, and uninstall.
- Live updates through `LauncherApps.Callback`.
- Automatic system-locale selection with English plus twelve translations.
- Galaxy S24 Ultra installation and testing documentation.
