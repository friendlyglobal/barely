# Barely roadmap

Barely should become more useful without becoming visually busy. The shared wallpaper-first command Home, local-first behavior, and optional Classic side pages remain product constraints rather than temporary limitations.

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
- [x] Add accessible manual ordering and resize controls to the hosted Favorites stack.

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

## 0.7 — Visual Coherence

Goal: make every existing path feel like one deliberate, daily-usable launcher before adding another broad feature set.

- [x] Replace wallpaper-derived theme shifts with a stable neutral palette, typography hierarchy, shape scale, and motion language.
- [x] Keep wallpaper context on content pages with tuned blur-aware scrims instead of disconnected gray or black backgrounds.
- [x] Refine Classic Home, Terminal, Favorites, Apps, Search, Settings, onboarding, widget catalog/editing, and long app-action lists.
- [x] Add a terminal-style local history drawer and learned query-to-app/shortcut ranking without contacts, commands, or AI prompts.
- [x] Make Terminal top actions float by default, offer optional contrast, and share an adjustable 0–32 dp radius with the prompt.
- [x] Provide local and full Terminal appearance resets with a restrained, squarer 12 dp default.
- [x] Constrain readable content on foldables and DeX, preserve bottom-up one-handed Search, and expose selected states to accessibility.
- [ ] Complete the final Galaxy S24 Ultra, TalkBack, keyboard/mouse, and reduced-motion acceptance pass before publishing 0.7.

## 0.8 — Android Power

Goal: use public Android capabilities well instead of depending on Samsung-only private APIs.

- Render adaptive icons correctly and offer an optional wallpaper-tinted monochrome icon mode.
- Make favorites, aliases, hidden apps, and pinned shortcuts fully profile-aware.
- Improve work-profile and Private Space state, empty states, and policy error handling.
- Expand quick settings through public Android settings panels and intents.
- Add a compact permission dashboard showing exactly which optional modules are active.
- Improve notification dots and media controls without indexing or persisting notification content.
- [x] Allow published App Shortcuts to be pinned as favorites.
- Add optional one-level favorite stacks; no nested folder hierarchy.
- Let users map a small set of Home gestures to search, notifications, Apps, Favorites, lock, or no action.
- [x] Export and import portable settings through Android's Storage Access Framework, excluding widget IDs and sensitive profile/module state.

## 0.9 — Everywhere and Trust

Goal: finish form-factor coverage, stop adding broad features, and produce a trustworthy release candidate.

- Improve keyboard focus order, shortcuts help, mouse hover, context actions, and drag behavior for DeX.
- Tune one-, two-, and three-pane layouts for foldables, tablets, landscape, and external displays.
- [x] Add page, heading, radio-choice, and modal-pane semantics while retaining 48 dp action targets.
- [x] Add a deterministic 1,000-item fuzzy-search p95 latency gate; device-level startup, discovery, refresh, and memory profiling remain for physical acceptance.
- [x] Document permissions, privacy boundaries, sensitive access, AI handoff, backup exclusions, signing, and vulnerability reporting.
- [x] Add versioned migration tests for legacy and current launcher preference schemas, including corrupt-value recovery.
- Run UI tests across supported Android versions and representative phone, foldable, tablet, and DeX sizes.
- Finish accessibility review and human review of all thirteen languages.
- [x] Produce optimized APK and AAB tasks with environment-only signing, certificate verification, checksums, and signing-key recovery documentation.
- [x] Add pull-request/main CI and establish build, unit-test, lint, and fuzzy-search release gates.

## 1.0 — Stable

Goal: ship a launcher that can be trusted as the daily Home app.

- [x] Stable `app.usefriendly.barely` application ID and environment-only production signing flow.
- [x] Reliable settings upgrades with documented signing migration and rollback behavior.
- [x] Wallpaper-first Home, fast local search, favorites, widgets, shortcuts, profiles, and optional system modules meeting the automated 0.9 quality gates.
- [x] Complete English documentation plus key-parity across the twelve supported system-locale translations.
- [x] No beta-only UI, placeholder package names, debug-signing fallback, or undocumented sensitive access in the production build.

## 1.1 — One-page widgets

Goal: make the existing Favorites/Widgets page feel native and dependable without adding another top-level surface.

- [x] Keep all hosted widgets on the single page left of Home, below favorites on compact windows and beside them in the existing foldable two-pane layout.
- [x] Separate live frame preview from provider option commits so resize stays responsive without repeatedly reinflating `RemoteViews`.
- [x] Preserve internal widget scrolling and return the gesture to the outer page only at the widget's own edge.
- [x] Open contextual widget actions on press-and-hold, expose provider reconfiguration only when Android marks it supported, and prevent the release gesture from opening provider content.
- [x] Protect focused resize handles from Android edge gestures without claiming the whole screen edge.
- [x] Retain deterministic four-column packing for compatible widgets side by side.
- [x] Cover resize commit policy and packing with local tests, then run lint, debug, optimized release, upgrade, and emulator runtime gates.

## 1.2 — One shared Home

Goal: make Command the minimal experience and Classic the detailed experience without maintaining two versions of Home.

- [x] Render the same shared command prompt, local ranking, history, wallpaper tint, opacity, and radius in both Home styles.
- [x] Hide redundant floating Apps and Settings actions in Classic, where those destinations remain adjacent pager surfaces.
- [x] Keep Favorites and the single Widgets surface exclusive to Classic without duplicating widget composition or persistence.
- [x] Preserve Command as the focused one-surface option and Classic as the richer three-surface option.
- [x] Fix and cover the Material 3 app-action sheet state crash on long press.

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
