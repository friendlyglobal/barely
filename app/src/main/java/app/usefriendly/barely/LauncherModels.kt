package app.usefriendly.barely

import android.content.ComponentName
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.os.UserHandle

enum class LauncherBackdrop {
    CLEAR,
    FROSTED,
    SEARCH,
}

enum class LauncherHomeMode {
    CLASSIC,
    TERMINAL,
}

enum class AppDrawerLayout {
    LIST,
    GRID,
}

enum class LauncherGestureAction {
    NONE,
    LOCK_SCREEN,
    NOTIFICATIONS,
    SEARCH,
    APPS,
}

enum class AssistantPreference(
    val packageName: String?,
) {
    CHATGPT("com.openai.chatgpt"),
    GEMINI("com.google.android.apps.bard"),
    CLAUDE("com.anthropic.claude"),
    ASK_EVERY_TIME(null),
}

internal object BarelyDefaults {
    const val TERMINAL_BACKGROUND_COLOR: Int = -0x1000000
    const val TERMINAL_BACKGROUND_OPACITY: Float = 0.42f
    const val TERMINAL_TOP_ACTION_BACKDROP: Boolean = false
    const val TERMINAL_CORNER_RADIUS: Int = 12
    const val TERMINAL_AESTHETIC: Boolean = false
    val APP_DRAWER_LAYOUT: AppDrawerLayout = AppDrawerLayout.LIST
    const val SHOW_APP_ICONS: Boolean = false
    const val SHOW_APP_GRID_LABELS: Boolean = true
    const val APP_GRID_COLUMNS: Int = 4
    const val APP_GRID_ROWS: Int = 6
}

data class LauncherSettings(
    val homeMode: LauncherHomeMode = LauncherHomeMode.TERMINAL,
    val terminalBackgroundColor: Int = BarelyDefaults.TERMINAL_BACKGROUND_COLOR,
    val terminalBackgroundOpacity: Float = BarelyDefaults.TERMINAL_BACKGROUND_OPACITY,
    val terminalTopActionBackdrop: Boolean = BarelyDefaults.TERMINAL_TOP_ACTION_BACKDROP,
    val terminalCornerRadius: Int = BarelyDefaults.TERMINAL_CORNER_RADIUS,
    val terminalAesthetic: Boolean = BarelyDefaults.TERMINAL_AESTHETIC,
    val doubleTapAction: LauncherGestureAction = LauncherGestureAction.LOCK_SCREEN,
    val swipeDownAction: LauncherGestureAction = LauncherGestureAction.NOTIFICATIONS,
    val frostedWallpaper: Boolean = true,
    val appDrawerLayout: AppDrawerLayout = BarelyDefaults.APP_DRAWER_LAYOUT,
    val showAppIcons: Boolean = BarelyDefaults.SHOW_APP_ICONS,
    val showAppGridLabels: Boolean = BarelyDefaults.SHOW_APP_GRID_LABELS,
    val appGridColumns: Int = BarelyDefaults.APP_GRID_COLUMNS,
    val appGridRows: Int = BarelyDefaults.APP_GRID_ROWS,
    val notificationDots: Boolean = false,
    val mediaControls: Boolean = false,
    val localSuggestions: Boolean = true,
    val showSearchHint: Boolean = true,
    val preferredAssistant: AssistantPreference = AssistantPreference.CHATGPT,
)

data class LauncherSearchLearning(
    val query: String,
    val targetKey: String,
    val selectionCount: Int,
    val lastSelectedAt: Long,
)

enum class LauncherProfileType {
    PERSONAL,
    WORK,
    PRIVATE,
    CLONE,
    OTHER,
}

data class LauncherProfile(
    val user: UserHandle,
    val userSerial: Long,
    val type: LauncherProfileType,
    val isLocked: Boolean,
    val hideEntryPointWhenLocked: Boolean,
)

data class LauncherApp(
    val label: String,
    val packageName: String,
    val component: ComponentName,
    val user: UserHandle,
    val userSerial: Long,
    val profileType: LauncherProfileType,
    val icon: Bitmap?,
) {
    val key: String = "${component.flattenToString()}@$userSerial"
    val searchTargetKey: String = "app:$key"
    val isPrivate: Boolean = profileType == LauncherProfileType.PRIVATE
}

data class LauncherShortcut(
    val info: ShortcutInfo,
    val owner: LauncherApp,
) {
    val label: String = info.shortLabel.toString()
    val description: String? = info.longLabel?.toString()
    val searchTargetKey: String = "shortcut:${owner.key}:${info.id}"
}

data class LauncherSnapshot(
    val apps: List<LauncherApp> = emptyList(),
    val shortcuts: List<LauncherShortcut> = emptyList(),
    val profiles: List<LauncherProfile> = emptyList(),
    val hasShortcutPermission: Boolean = false,
) {
    val privateSpace: LauncherProfile? = profiles.firstOrNull {
        it.type == LauncherProfileType.PRIVATE
    }
}
