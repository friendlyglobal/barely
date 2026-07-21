# Changelog

All notable changes to Barely are documented in this file.

## 0.5 — Unreleased

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

### Planned before release

- Prioritized the production signing strategy before broader adoption.
- Continue the 0.5 delivery with manual ordering, hidden apps, aliases, and system-backed commands.

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
