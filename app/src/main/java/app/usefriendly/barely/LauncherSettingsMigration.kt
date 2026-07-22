package app.usefriendly.barely

internal const val CURRENT_SETTINGS_SCHEMA = 4

internal data class StoredLauncherSettings(
    val schemaVersion: Int = 1,
    val homeMode: String? = null,
    val terminalBackgroundColor: Int = BarelyDefaults.TERMINAL_BACKGROUND_COLOR,
    val terminalBackgroundOpacity: Float = BarelyDefaults.TERMINAL_BACKGROUND_OPACITY,
    val terminalTopActionBackdrop: Boolean = BarelyDefaults.TERMINAL_TOP_ACTION_BACKDROP,
    val terminalCornerRadius: Int = BarelyDefaults.TERMINAL_CORNER_RADIUS,
    val terminalAesthetic: Boolean = BarelyDefaults.TERMINAL_AESTHETIC,
    val doubleTapAction: String? = null,
    val swipeDownAction: String? = null,
    val legacyDoubleTapToLock: Boolean = true,
    val legacySwipeDownForNotifications: Boolean = true,
    val frostedWallpaper: Boolean = true,
    val appDrawerLayout: String? = null,
    val showAppIcons: Boolean = BarelyDefaults.SHOW_APP_ICONS,
    val showAppGridLabels: Boolean = BarelyDefaults.SHOW_APP_GRID_LABELS,
    val appGridColumns: Int = BarelyDefaults.APP_GRID_COLUMNS,
    val appGridRows: Int = BarelyDefaults.APP_GRID_ROWS,
    val notificationDots: Boolean = false,
    val mediaControls: Boolean = false,
    val localSuggestions: Boolean = true,
    val showSearchHint: Boolean = true,
    val preferredAssistant: String? = null,
)

internal fun decodeLauncherSettings(stored: StoredLauncherSettings): LauncherSettings =
    LauncherSettings(
        homeMode = stored.homeMode.enumOrNull<LauncherHomeMode>()
            ?: LauncherHomeMode.TERMINAL,
        terminalBackgroundColor = stored.terminalBackgroundColor,
        terminalBackgroundOpacity = stored.terminalBackgroundOpacity
            .takeIf(Float::isFinite)
            ?.coerceIn(0f, 1f)
            ?: BarelyDefaults.TERMINAL_BACKGROUND_OPACITY,
        terminalTopActionBackdrop = stored.terminalTopActionBackdrop,
        terminalCornerRadius = stored.terminalCornerRadius.coerceIn(0, 32),
        terminalAesthetic = stored.terminalAesthetic,
        doubleTapAction = stored.doubleTapAction.enumOrNull<LauncherGestureAction>()
            ?: if (stored.legacyDoubleTapToLock) {
                LauncherGestureAction.LOCK_SCREEN
            } else {
                LauncherGestureAction.NONE
            },
        swipeDownAction = stored.swipeDownAction.enumOrNull<LauncherGestureAction>()
            ?: if (stored.legacySwipeDownForNotifications) {
                LauncherGestureAction.NOTIFICATIONS
            } else {
                LauncherGestureAction.NONE
            },
        frostedWallpaper = stored.frostedWallpaper,
        appDrawerLayout = stored.appDrawerLayout.enumOrNull<AppDrawerLayout>()
            ?: BarelyDefaults.APP_DRAWER_LAYOUT,
        showAppIcons = stored.showAppIcons,
        showAppGridLabels = stored.showAppGridLabels,
        appGridColumns = stored.appGridColumns.coerceIn(
            MIN_APP_GRID_COLUMNS,
            MAX_APP_GRID_COLUMNS,
        ),
        appGridRows = stored.appGridRows.coerceIn(
            MIN_APP_GRID_ROWS,
            MAX_APP_GRID_ROWS,
        ),
        notificationDots = stored.notificationDots,
        mediaControls = stored.mediaControls,
        localSuggestions = stored.localSuggestions,
        showSearchHint = stored.showSearchHint,
        preferredAssistant = stored.preferredAssistant.enumOrNull<AssistantPreference>()
            ?: AssistantPreference.CHATGPT,
    )

private inline fun <reified T : Enum<T>> String?.enumOrNull(): T? =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }

internal const val MIN_APP_GRID_COLUMNS = 3
internal const val MAX_APP_GRID_COLUMNS = 6
internal const val MIN_APP_GRID_ROWS = 4
internal const val MAX_APP_GRID_ROWS = 8
