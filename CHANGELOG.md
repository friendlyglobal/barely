# Changelog

All notable changes to Launchly are documented in this file.

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
