# Changelog

All notable changes to Barely are documented in this file.

## 1.3.0 — 2026-07-22

### All apps

- Added one shared All apps renderer with a minimal list or configurable grid in both Command and Classic.
- Added one optional app-icon switch for All apps, shared Home command results, published shortcuts, and recent command history while preserving the 1.2 text-only presentation as the upgrade-safe default.
- Added discrete grid sliders for three to six columns and four to eight visible rows, with a one-tap reset to the balanced 4 × 6 default.
- Kept app launch, long press, notification dots, Private Space, search entry, and foldable list adaptation on the same data and interaction path in every layout.
- Matched the All apps search control to the shared Home command bar's 20 dp bottom spacing through one visual token.
- Applied the global search/command radius consistently to command result surfaces, history entries, and the draggable history header.
- Aligned the All apps search control to Material 3's 56 dp field, 24 dp leading icon, 16 dp inset and icon-to-text spacing, and clipped its pressed state to the configured shape.
- Unified the app counter with the header foreground instead of giving it an unrelated muted color.

### Motion

- Replaced the instant Classic side-page reset with the pager's restrained spring transition, so Home visibly moves from Favorites/Widgets or Apps back to the shared center surface.
- Repeated Home requests cancel and replace the active transition instead of queuing movement.

### Settings and release

- Persisted and exported the All apps layout, icon, column, and row choices with bounded migration and corrupt-value recovery coverage.
- Added localized controls for every supported system language and TalkBack labels for both density sliders.
- Updated the application to version code 17 and version name 1.3.0.

## 1.2.0 — 2026-07-22

### Home

- Made Command and Classic render the same shared Home component, including the bottom prompt, local fuzzy ranking, recent-search history, wallpaper tint, opacity, and corner radius.
- Kept Command as the minimal single-surface style with its floating Apps and Settings actions.
- Kept Classic as the detailed style with the existing Favorites/Widgets page on the left and All apps page on the right; its shared Home hides redundant top actions.
- Preserved local ranking and history across both Home styles without adding a second implementation or another widget surface.

### Fixed

- Fixed the app action sheet crash triggered by long-pressing an app or search result. The sheet now enables only the valid hidden and fully expanded states, so long shortcut lists remain scrollable and reachable.
- Added unit coverage for the app action sheet state contract.

### Release

- Updated the application to version code 16 and version name 1.2.0.

## 1.1.0 — 2026-07-22

### Widgets

- Kept favorites and every hosted widget in one deliberate page to the left of Home; no extra widget pages or carousel were added.
- Removed competing size animation and stopped notifying widget providers on every drag frame. The frame now follows the finger continuously while the provider receives one committed size at gesture end, preventing `RemoteViews` reinflation flicker.
- Added an Android-view gesture bridge so scrollable content inside a widget gets first refusal while it can scroll, then hands the gesture back to the surrounding Favorites page at its edge.
- Added a native-feeling long-press menu for every hosted widget with Resize and arrange, provider Settings when Android exposes reconfiguration, App info, and Remove. Releasing after the long press no longer triggers the widget underneath.
- Expanded the right and bottom resize targets to accessible sizes and excluded only those focused handles from Android system gestures, preventing edge-back conflicts during editing.
- Preserved the four-column packing model, including compatible widgets side by side and the existing two-pane adaptation on foldables.
- Added unit coverage for committed-versus-preview provider sizing and retained row-packing coverage.

### Release

- Updated the application to version code 15 and version name 1.1.0.

## 1.0.0 — 2026-07-22

### Stable

- Froze the wallpaper-first Classic and Command experience after the 0.9 release gates; no new permission, backend, analytics, or account system was added.
- Updated stable AndroidX Core, Activity, and Coroutines dependencies and retained the pinned Compose/Material versions required by the current UI APIs.
- Regenerated the Gradle wrapper from Gradle 9.3.1, verified its official wrapper JAR checksum, and pinned the distribution SHA-256.
- Pinned every GitHub Action to a reviewed commit and added weekly Dependabot checks for Gradle and workflow dependencies.
- Corrected Indonesian resource packaging and removed obsolete Android version qualifiers from adaptive launcher icons.
- Added final upgrade, rollback, signing, and recovery documentation for daily-driver installs.
- Updated the application to version code 14 and version name 1.0.0.

## 0.9.0 — 2026-07-21

### Changed

- Added a versioned settings decoder with tested migration from legacy gesture switches and safe recovery from corrupt enum, opacity, and radius values.
- Added configuration-aware resource reads and clearer accessibility semantics for page headings, modal panes, and mutually exclusive Home/assistant choices.
- Replaced the deprecated Material 3 modal-sheet state API.
- Added an explicit Android data-extraction policy so launcher preferences are excluded from cloud backup and device transfer as well as `allowBackup=false`.
- Added a deterministic fuzzy-search latency gate over a synthetic 1,000-item launcher index.
- Added optimized R8/resource-shrunk release APK and AAB builds. Release signing is environment-driven and never falls back to the debug key.
- Added an end-to-end release verification script that runs tests/lint/builds, verifies the APK certificate, and writes SHA-256 checksums.
- Added GitHub Actions verification for pull requests and `main`, plus a public security and privacy model.
- Updated the application to version code 13 and version name 0.9.0.

## 0.8.0 — 2026-07-21

### Added

- Local-first AI fallback: an installed assistant appears only when apps, shortcuts, contacts, calculations, and local commands produce no result.
- ChatGPT, Gemini, and Claude detection plus a preferred-assistant choice in onboarding and Settings.
- A local `codex` command that opens the installed ChatGPT app without adding an API key or network permission to Barely.
- Published App Shortcuts can be pinned directly to Favorites from the app actions sheet.
- Configurable double-tap and swipe-down Home actions with None, Lock/Notifications, Search, and All apps choices where they apply.
- Portable JSON settings export and import through Android's document picker; widgets, grants, profiles, favorites, and local history are deliberately excluded.

### Changed

- Shortened the Brazilian Portuguese Command Home label to `Comando` so the Home-style card stays on one line.
- AI handoff uses the selected installed app and remains an explicit tap/Enter action; Barely still cannot read the response.

### Privacy

- Barely still declares no Internet permission, stores no assistant prompt history, and contains no API key or relay server.

## 0.7.2 — 2026-07-21

### Fixed

- Open Gesture access directly on the Barely gestures service detail page when the device permits it.
- On Samsung devices that protect the detail intent, open the Installed apps accessibility list with Barely gestures selected; other devices fall back to Accessibility with Barely gestures highlighted.

## 0.7.1 — 2026-07-21

### Changed

- Renamed the user-facing Terminal style to Command while preserving the internal preference identifier for seamless upgrades.
- Made Command the default Home style for new installations while preserving the saved choice of existing users.
- Made Command visually clean by default and moved prompts, arrows, and monospace typography behind an optional Terminal aesthetics switch.
- Replaced Command's textual top actions with compact, accessible app-grid and settings icons.
- Applied the configured Command corner radius to the All apps search entry and the full Search input.
- Reworked Command history into a finger-tracked vertical transition that snaps open or closed from drag distance and velocity.

## 0.7 — 2026-07-21

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
