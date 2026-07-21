package com.example.minimallauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.LauncherUserInfo
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.os.UserManager
import androidx.core.graphics.drawable.toBitmap
import androidx.core.content.edit
import androidx.annotation.RequiresApi
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

    private val profileReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = onChanged()
    }
    private var profileReceiverRegistered = false

    init {
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        registerProfileReceiver()
    }

    suspend fun load(): LauncherSnapshot = withContext(Dispatchers.Default) {
        val collator = Collator.getInstance(Locale.getDefault()).apply {
            strength = Collator.PRIMARY
        }
        val profiles = launcherApps.profiles.map(::profileForUser)
        val apps = profiles.flatMap { profile ->
            if (profile.type == LauncherProfileType.PRIVATE && profile.isLocked) {
                return@flatMap emptyList()
            }
            val user = profile.user
            launcherApps.getActivityList(null, user)
                .asSequence()
                .filterNot { it.applicationInfo.packageName == appContext.packageName }
                .map { activity ->
                    LauncherApp(
                        label = activity.label.toString(),
                        packageName = activity.applicationInfo.packageName,
                        component = activity.componentName,
                        user = user,
                        userSerial = profile.userSerial,
                        profileType = profile.type,
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
            profiles
                .filterNot { it.type == LauncherProfileType.PRIVATE && it.isLocked }
                .flatMap { profile -> loadShortcuts(profile.user, apps) }
                .sortedWith { left, right -> collator.compare(left.label, right.label) }
        } else {
            emptyList()
        }

        LauncherSnapshot(
            apps = apps,
            shortcuts = shortcuts,
            profiles = profiles,
            hasShortcutPermission = canReadShortcuts,
        )
    }

    private fun profileForUser(user: UserHandle): LauncherProfile {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            runCatching { launcherApps.getLauncherUserInfo(user) }.getOrNull()
        } else {
            null
        }
        val type = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && info != null
        ) {
            profileType(info)
        } else if (user == android.os.Process.myUserHandle()) {
            LauncherProfileType.PERSONAL
        } else {
            LauncherProfileType.OTHER
        }
        val isLocked = runCatching { userManager.isQuietModeEnabled(user) }.getOrDefault(false)
        val hideEntryPointWhenLocked = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA && info != null
        ) {
            info.userConfig.getBoolean(LauncherUserInfo.PRIVATE_SPACE_ENTRYPOINT_HIDDEN, false)
        } else {
            false
        }
        return LauncherProfile(
            user = user,
            userSerial = userManager.getSerialNumberForUser(user),
            type = type,
            isLocked = isLocked,
            hideEntryPointWhenLocked = hideEntryPointWhenLocked,
        )
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun profileType(info: LauncherUserInfo): LauncherProfileType =
        when (info.userType) {
            UserManager.USER_TYPE_PROFILE_PRIVATE -> LauncherProfileType.PRIVATE
            UserManager.USER_TYPE_PROFILE_MANAGED -> LauncherProfileType.WORK
            UserManager.USER_TYPE_PROFILE_CLONE -> LauncherProfileType.CLONE
            else -> LauncherProfileType.OTHER
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

    fun isPrivateSpaceExpanded(): Boolean =
        preferences.getBoolean(PRIVATE_SPACE_EXPANDED_KEY, true)

    fun setPrivateSpaceExpanded(expanded: Boolean) {
        preferences.edit { putBoolean(PRIVATE_SPACE_EXPANDED_KEY, expanded) }
    }

    fun areNotificationDotsEnabled(): Boolean =
        preferences.getBoolean(NOTIFICATION_DOTS_KEY, false)

    fun setNotificationDotsEnabled(enabled: Boolean) {
        preferences.edit { putBoolean(NOTIFICATION_DOTS_KEY, enabled) }
    }

    fun areMediaControlsEnabled(): Boolean =
        preferences.getBoolean(MEDIA_CONTROLS_KEY, false)

    fun setMediaControlsEnabled(enabled: Boolean) {
        preferences.edit { putBoolean(MEDIA_CONTROLS_KEY, enabled) }
    }

    fun setPrivateSpaceLocked(profile: LauncherProfile, locked: Boolean): Boolean {
        if (profile.type != LauncherProfileType.PRIVATE) return false
        val changedImmediately = userManager.requestQuietModeEnabled(locked, profile.user)
        if (changedImmediately) onChanged()
        return changedImmediately
    }

    private fun registerProfileReceiver() {
        val filter = IntentFilter().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                addAction(Intent.ACTION_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
                addAction(Intent.ACTION_PROFILE_REMOVED)
            } else {
                addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
                addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(profileReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            appContext.registerReceiver(profileReceiver, filter)
        }
        profileReceiverRegistered = true
    }

    fun close() {
        launcherApps.unregisterCallback(callback)
        if (profileReceiverRegistered) {
            appContext.unregisterReceiver(profileReceiver)
            profileReceiverRegistered = false
        }
    }

    private companion object {
        const val FAVORITES_KEY = "favorites"
        const val GESTURE_COACH_KEY = "gesture_coach_seen"
        const val PRIVATE_SPACE_EXPANDED_KEY = "private_space_expanded"
        const val NOTIFICATION_DOTS_KEY = "notification_dots_enabled"
        const val MEDIA_CONTROLS_KEY = "media_controls_enabled"
        const val ICON_SIZE_PX = 144
    }
}
