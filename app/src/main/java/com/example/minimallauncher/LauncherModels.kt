package com.example.minimallauncher

import android.content.ComponentName
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.os.UserHandle

data class LauncherApp(
    val label: String,
    val packageName: String,
    val component: ComponentName,
    val user: UserHandle,
    val userSerial: Long,
    val icon: Bitmap?,
) {
    val key: String = "${component.flattenToString()}@$userSerial"
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
    val hasShortcutPermission: Boolean = false,
)
