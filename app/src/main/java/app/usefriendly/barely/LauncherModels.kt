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

data class LauncherSettings(
    val homeMode: LauncherHomeMode = LauncherHomeMode.CLASSIC,
    val terminalBackgroundColor: Int = 0xFF000000.toInt(),
    val terminalBackgroundOpacity: Float = 0.42f,
    val doubleTapToLock: Boolean = true,
    val swipeDownForNotifications: Boolean = true,
    val frostedWallpaper: Boolean = true,
    val notificationDots: Boolean = false,
    val mediaControls: Boolean = false,
    val localSuggestions: Boolean = true,
    val showSearchHint: Boolean = true,
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
    val isPrivate: Boolean = profileType == LauncherProfileType.PRIVATE
}

data class LauncherShortcut(
    val info: ShortcutInfo,
    val owner: LauncherApp,
) {
    val label: String = info.shortLabel.toString()
    val description: String? = info.longLabel?.toString()
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
