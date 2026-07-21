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

internal object BarelyDefaults {
    const val TERMINAL_BACKGROUND_COLOR: Int = -0x1000000
    const val TERMINAL_BACKGROUND_OPACITY: Float = 0.42f
    const val TERMINAL_TOP_ACTION_BACKDROP: Boolean = false
    const val TERMINAL_CORNER_RADIUS: Int = 12
}

data class LauncherSettings(
    val homeMode: LauncherHomeMode = LauncherHomeMode.CLASSIC,
    val terminalBackgroundColor: Int = BarelyDefaults.TERMINAL_BACKGROUND_COLOR,
    val terminalBackgroundOpacity: Float = BarelyDefaults.TERMINAL_BACKGROUND_OPACITY,
    val terminalTopActionBackdrop: Boolean = BarelyDefaults.TERMINAL_TOP_ACTION_BACKDROP,
    val terminalCornerRadius: Int = BarelyDefaults.TERMINAL_CORNER_RADIUS,
    val doubleTapToLock: Boolean = true,
    val swipeDownForNotifications: Boolean = true,
    val frostedWallpaper: Boolean = true,
    val notificationDots: Boolean = false,
    val mediaControls: Boolean = false,
    val localSuggestions: Boolean = true,
    val showSearchHint: Boolean = true,
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
