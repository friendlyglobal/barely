# Barely roadmap

Barely should become more useful without becoming visually busy. The empty wallpaper Home page, local-first behavior, and three-surface navigation remain product constraints rather than temporary limitations.

## Product rules

- Home stays empty by default. New information belongs in Favorites, Apps, Search, or an optional module.
- Features work offline whenever Android exposes a local API for them.
- Sensitive permissions remain optional, narrowly explained, and independently revocable.
- No analytics, advertising SDK, account system, AI proxy, or Barely backend.
- Prefer Android system pickers, roles, panels, and intents over recreating system UI.
- Add no permanent top-level page beyond Favorites, Home, and Apps.
- Every visible feature needs a clear daily-use case and a way to turn it off.

## 0.5 — Daily Flow

Goal: make the first second of using the launcher faster while settling the app identity before more people install it.

### Release identity — priority zero

- Use the permanent `app.usefriendly.barely` application ID beginning with 0.5.
- Add a production signing configuration that reads secrets from the local environment and never commits a keystore or passwords.
- Document the one-time migration consequence: Android treats Barely as a different app from the Launchly prototype, so the default launcher role and widgets must be configured again.
- Keep the debug build available for contributors while producing a separately signed release artifact.

### Zero-query search

- [x] Show useful recent launches and successful app searches before typing.
- [x] Learn only from launches initiated inside Barely. Do not request Android Usage Access.
- [x] Use a small decayed score so recent behavior matters without permanently fixing old habits.
- [x] Provide visible controls to disable and clear local launch history.
- [x] Never include locked Private Space content.

### Widget catalog

- [x] Group installed widgets by publisher instead of exposing a flat provider list.
- [x] Search by app or widget name and display published previews with proportional grid sizes.
- [x] Use Android 15 generated previews, Android 12 scalable preview layouts, static preview images, and icon fallbacks progressively.
- [ ] Add accessible manual ordering and resize controls to the hosted Favorites stack.

### Lightweight organization

- Reorder favorites and widgets with a deliberate edit mode and accessible move actions.
- Hide apps from normal Apps and Search results, with a clearly named management screen.
- Make it explicit that hiding is organization, not security; Private Space remains the secure option.
- Add local aliases and search keywords, such as mapping a banking app to “bank”.
- Scope hidden state and aliases by Android profile.

### Command palette expansion

- Add system-backed timers, alarms, navigation, calling, and messaging actions where Android provides a safe intent.
- Require a tap before any external action; typing alone never sends data or starts a call.
- Keep calculation, conversion, matching, and ranking fully local.

### 0.5 exit criteria

- No new mandatory permission.
- Zero-query recommendations can be disabled and reset.
- Favorite order, hidden apps, and aliases survive process death.
- TalkBack, keyboard, mouse, phone, foldable, and DeX paths are covered.
- The permanent application ID and signing strategy are decided before the release.

## 0.6 — Intentional Home

Goal: add a digital-declutter interface without compromising Classic Home or local privacy.

- [x] Offer Classic and Terminal choices during onboarding and in settings.
- [x] Keep Terminal wallpaper-first with an optional local color and opacity layer.
- [x] Reuse the local fuzzy index for apps, App Shortcuts, and commands instead of creating a second search engine.
- [x] Limit Terminal to three suggestions and support touch, keyboard, mouse, and DeX.
- [x] Provide explicit `:apps`, `:settings`, and `:classic` exits.
- [x] Resize widgets live from their right and bottom edges without covering provider content.
- [x] Pack compatible half-width widgets side by side after editing without introducing a free-form, fragile canvas.
- [x] Keep Classic Search bottom-up and open its closest, best-ranked result from the phone keyboard action.
- [x] Smooth Home/Search transitions and preserve the wallpaper when system blur is unavailable.
- [ ] Complete TalkBack, RTL, reduced-motion, foldable, and physical Galaxy S24 Ultra review before release.

## 0.7 — Android Power

Goal: use public Android capabilities well instead of depending on Samsung-only private APIs.

- Render adaptive icons correctly and offer an optional wallpaper-tinted monochrome icon mode.
- Make favorites, aliases, hidden apps, and pinned shortcuts fully profile-aware.
- Improve work-profile and Private Space state, empty states, and policy error handling.
- Expand quick settings through public Android settings panels and intents.
- Add a compact permission dashboard showing exactly which optional modules are active.
- Improve notification dots and media controls without indexing or persisting notification content.
- Allow published App Shortcuts to be pinned as favorites.
- Add optional one-level favorite stacks; no nested folder hierarchy.
- Let users map a small set of Home gestures to search, notifications, Apps, Favorites, lock, or no action.
- Export and import portable settings through Android's Storage Access Framework, excluding widget IDs and sensitive profile/module state.

## 0.8 — Everywhere

Goal: make the same minimal interaction model excellent on every Android form factor.

- Complete keyboard focus order, shortcuts help, mouse hover, context actions, and drag behavior for DeX.
- Tune one-, two-, and three-pane layouts for foldables, tablets, landscape, and external displays.
- Respect hinges, cutouts, large fonts, RTL, high contrast, reduced motion, and touch-target guidance.
- Add performance benchmarks for cold start, app discovery, shortcut refresh, search latency, and memory.
- Keep app and shortcut indexes in memory and update them incrementally through platform callbacks.

## 0.9 — Trust

Goal: stop adding broad features and prepare a release candidate.

- Complete a permissions, privacy, threat-model, and dependency review.
- Test upgrades, profile changes, revoked permissions, locked profiles, missing apps, and corrupted preferences.
- Add migration tests for every persisted preference version.
- Run UI tests across the supported Android versions and representative phone, foldable, tablet, and DeX sizes.
- Finish accessibility review and human review of all thirteen languages.
- Produce reproducible signed APK and AAB artifacts with checksums and recovery documentation for the signing key.
- Establish cold-start, search-latency, memory, and crash-free release gates.

## 1.0 — Stable

Goal: ship a launcher that can be trusted as the daily Home app.

- Stable application ID and production signing key.
- Reliable upgrades with documented migration and rollback behavior.
- Wallpaper-first Home, fast local search, favorites, widgets, shortcuts, profiles, and optional system modules all meeting the 0.9 quality gates.
- Complete English documentation plus the twelve supported system-locale translations.
- No beta-only UI, placeholder package names, debug signing, or undocumented sensitive access.

## Deliberately out of scope

- A built-in online AI client, bundled API keys, or a Barely server.
- News feeds, social feeds, ads, analytics, or engagement notifications.
- Notification-content search or permanent notification history.
- Pretending that hidden apps are secure; Android Private Space is the security boundary.
- Root-only behavior, private Samsung APIs, or accessibility automation outside the two explicit Home gestures.
- A plugin framework or architecture layer without a concrete user-facing need.

## Platform references

- [LauncherApps](https://developer.android.com/reference/android/content/pm/LauncherApps)
- [App Shortcuts](https://developer.android.com/develop/ui/views/launch/shortcuts)
- [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files)
- [Adaptive icons](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive)
- [Compose accessibility](https://developer.android.com/develop/ui/compose/accessibility)
- [Compose lazy lists and item animations](https://developer.android.com/develop/ui/compose/lists)
