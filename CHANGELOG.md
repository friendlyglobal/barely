# Changelog

All notable changes to Launchly are documented in this file.

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
