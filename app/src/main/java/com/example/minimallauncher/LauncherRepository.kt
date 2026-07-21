package com.example.minimallauncher

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.os.UserManager
import androidx.core.graphics.drawable.toBitmap
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

class LauncherRepository(
    context: Context,
    private val onChanged: () -> Unit,
) {
    private val appContext = context.applicationContext
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)
    private val userManager = appContext.getSystemService(UserManager::class.java)
    private val preferences = appContext.getSharedPreferences("launcher", Context.MODE_PRIVATE)
    private val densityDpi = appContext.resources.displayMetrics.densityDpi

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String, user: UserHandle) = onChanged()
        override fun onPackageAdded(packageName: String, user: UserHandle) = onChanged()
        override fun onPackageChanged(packageName: String, user: UserHandle) = onChanged()

        override fun onPackagesAvailable(
            packageNames: Array<out String>,
            user: UserHandle,
            replacing: Boolean,
        ) = onChanged()

        override fun onPackagesUnavailable(
            packageNames: Array<out String>,
            user: UserHandle,
            replacing: Boolean,
        ) = onChanged()

        override fun onShortcutsChanged(
            packageName: String,
            shortcuts: MutableList<ShortcutInfo>,
            user: UserHandle,
        ) = onChanged()
    }

    init {
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
    }

    suspend fun load(): LauncherSnapshot = withContext(Dispatchers.Default) {
        val collator = Collator.getInstance(Locale.getDefault()).apply {
            strength = Collator.PRIMARY
        }
        val profiles = userManager.userProfiles
        val apps = profiles.flatMap { user ->
            val serial = userManager.getSerialNumberForUser(user)
            launcherApps.getActivityList(null, user)
                .asSequence()
                .filterNot { it.applicationInfo.packageName == appContext.packageName }
                .map { activity ->
                    LauncherApp(
                        label = activity.label.toString(),
                        packageName = activity.applicationInfo.packageName,
                        component = activity.componentName,
                        user = user,
                        userSerial = serial,
                        icon = runCatching {
                            activity.getBadgedIcon(densityDpi).toBitmap(
                                width = ICON_SIZE_PX,
                                height = ICON_SIZE_PX,
                                config = Bitmap.Config.ARGB_8888,
                            )
                        }.getOrNull(),
                    )
                }
                .toList()
        }.sortedWith { left, right -> collator.compare(left.label, right.label) }

        val canReadShortcuts = launcherApps.hasShortcutHostPermission()
        val shortcuts = if (canReadShortcuts) {
            profiles.flatMap { user -> loadShortcuts(user, apps) }
                .sortedWith { left, right -> collator.compare(left.label, right.label) }
        } else {
            emptyList()
        }

        LauncherSnapshot(
            apps = apps,
            shortcuts = shortcuts,
            hasShortcutPermission = canReadShortcuts,
        )
    }

    private fun loadShortcuts(
        user: UserHandle,
        apps: List<LauncherApp>,
    ): List<LauncherShortcut> {
        val flags = LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                LauncherApps.ShortcutQuery.FLAG_MATCH_CACHED
            } else {
                0
            }
        val query = LauncherApps.ShortcutQuery().setQueryFlags(flags)

        return runCatching { launcherApps.getShortcuts(query, user).orEmpty() }
            .getOrDefault(emptyList())
            .mapNotNull { shortcut ->
                val owner = apps.firstOrNull {
                    it.user == user && it.component == shortcut.activity
                } ?: apps.firstOrNull {
                    it.user == user && it.packageName == shortcut.`package`
                }
                owner?.let { LauncherShortcut(shortcut, it) }
            }
    }

    fun launch(app: LauncherApp) {
        launcherApps.startMainActivity(app.component, app.user, null, null)
    }

    fun launch(shortcut: LauncherShortcut) {
        launcherApps.startShortcut(shortcut.info, null, null)
    }

    fun showAppInfo(app: LauncherApp) {
        launcherApps.startAppDetailsActivity(app.component, app.user, null, null)
    }

    fun favoriteKeys(): Set<String> =
        preferences.getStringSet(FAVORITES_KEY, emptySet()).orEmpty().toSet()

    fun toggleFavorite(app: LauncherApp): Set<String> {
        val favorites = favoriteKeys().toMutableSet()
        if (!favorites.add(app.key)) favorites.remove(app.key)
        preferences.edit { putStringSet(FAVORITES_KEY, favorites) }
        return favorites
    }

    fun isGestureCoachSeen(): Boolean = preferences.getBoolean(GESTURE_COACH_KEY, false)

    fun markGestureCoachSeen() {
        preferences.edit { putBoolean(GESTURE_COACH_KEY, true) }
    }

    fun close() {
        launcherApps.unregisterCallback(callback)
    }

    private companion object {
        const val FAVORITES_KEY = "favorites"
        const val GESTURE_COACH_KEY = "gesture_coach_seen"
        const val ICON_SIZE_PX = 144
    }
}
