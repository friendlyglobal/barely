package app.usefriendly.barely

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
import kotlin.math.ln

class LauncherRepository(
    context: Context,
    private val onChanged: () -> Unit,
) {
    private val appContext = context.applicationContext
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)
    private val userManager = appContext.getSystemService(UserManager::class.java)
    private val preferences = appContext.getSharedPreferences("barely", Context.MODE_PRIVATE)
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
        if (localSuggestionsEnabled() && !app.isPrivate) recordAppLaunch(app)
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

    fun isOnboardingComplete(): Boolean = preferences.getBoolean(
        ONBOARDING_COMPLETE_KEY,
        preferences.getBoolean(GESTURE_COACH_KEY, false),
    )

    fun markOnboardingComplete() {
        preferences.edit { putBoolean(ONBOARDING_COMPLETE_KEY, true) }
    }

    fun isPrivateSpaceExpanded(): Boolean =
        preferences.getBoolean(PRIVATE_SPACE_EXPANDED_KEY, true)

    fun setPrivateSpaceExpanded(expanded: Boolean) {
        preferences.edit { putBoolean(PRIVATE_SPACE_EXPANDED_KEY, expanded) }
    }

    fun launcherSettings(): LauncherSettings = LauncherSettings(
        homeMode = preferences.getString(HOME_MODE_KEY, null)
            ?.let { value -> runCatching { LauncherHomeMode.valueOf(value) }.getOrNull() }
            ?: LauncherHomeMode.CLASSIC,
        terminalBackgroundColor = preferences.getInt(
            TERMINAL_BACKGROUND_COLOR_KEY,
            BarelyDefaults.TERMINAL_BACKGROUND_COLOR,
        ),
        terminalBackgroundOpacity = preferences.getFloat(
            TERMINAL_BACKGROUND_OPACITY_KEY,
            BarelyDefaults.TERMINAL_BACKGROUND_OPACITY,
        ).coerceIn(0f, 1f),
        terminalTopActionBackdrop = preferences.getBoolean(
            TERMINAL_TOP_ACTION_BACKDROP_KEY,
            BarelyDefaults.TERMINAL_TOP_ACTION_BACKDROP,
        ),
        terminalCornerRadius = preferences.getInt(
            TERMINAL_CORNER_RADIUS_KEY,
            BarelyDefaults.TERMINAL_CORNER_RADIUS,
        )
            .coerceIn(MIN_TERMINAL_CORNER_RADIUS, MAX_TERMINAL_CORNER_RADIUS),
        doubleTapToLock = preferences.getBoolean(DOUBLE_TAP_LOCK_KEY, true),
        swipeDownForNotifications = preferences.getBoolean(SWIPE_NOTIFICATIONS_KEY, true),
        frostedWallpaper = preferences.getBoolean(FROSTED_WALLPAPER_KEY, true),
        notificationDots = preferences.getBoolean(NOTIFICATION_DOTS_KEY, false),
        mediaControls = preferences.getBoolean(MEDIA_CONTROLS_KEY, false),
        localSuggestions = preferences.getBoolean(LOCAL_SUGGESTIONS_KEY, true),
        showSearchHint = preferences.getBoolean(SHOW_SEARCH_HINT_KEY, true),
    )

    fun setLauncherSettings(settings: LauncherSettings) {
        preferences.edit {
            putString(HOME_MODE_KEY, settings.homeMode.name)
            putInt(TERMINAL_BACKGROUND_COLOR_KEY, settings.terminalBackgroundColor)
            putFloat(
                TERMINAL_BACKGROUND_OPACITY_KEY,
                settings.terminalBackgroundOpacity.coerceIn(0f, 1f),
            )
            putBoolean(TERMINAL_TOP_ACTION_BACKDROP_KEY, settings.terminalTopActionBackdrop)
            putInt(
                TERMINAL_CORNER_RADIUS_KEY,
                settings.terminalCornerRadius.coerceIn(
                    MIN_TERMINAL_CORNER_RADIUS,
                    MAX_TERMINAL_CORNER_RADIUS,
                ),
            )
            putBoolean(DOUBLE_TAP_LOCK_KEY, settings.doubleTapToLock)
            putBoolean(SWIPE_NOTIFICATIONS_KEY, settings.swipeDownForNotifications)
            putBoolean(FROSTED_WALLPAPER_KEY, settings.frostedWallpaper)
            putBoolean(NOTIFICATION_DOTS_KEY, settings.notificationDots)
            putBoolean(MEDIA_CONTROLS_KEY, settings.mediaControls)
            putBoolean(LOCAL_SUGGESTIONS_KEY, settings.localSuggestions)
            putBoolean(SHOW_SEARCH_HINT_KEY, settings.showSearchHint)
        }
    }

    fun recommendedAppKeys(limit: Int = 5): List<String> {
        if (!localSuggestionsEnabled()) return emptyList()
        val now = System.currentTimeMillis()
        return preferences.all.keys
            .asSequence()
            .filter { it.startsWith(USAGE_COUNT_PREFIX) }
            .map { countKey ->
                val appKey = countKey.removePrefix(USAGE_COUNT_PREFIX)
                val count = preferences.getInt(countKey, 0)
                val lastUsed = preferences.getLong(USAGE_LAST_PREFIX + appKey, 0L)
                val ageHours = ((now - lastUsed).coerceAtLeast(0L) / 3_600_000.0)
                val recency = when {
                    ageHours < 3 -> 8.0
                    ageHours < 24 -> 5.0
                    ageHours < 72 -> 3.0
                    ageHours < 168 -> 1.0
                    else -> 0.0
                }
                appKey to (ln(count + 1.0) * 2.0 + recency)
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(limit)
            .toList()
    }

    fun recentAppSearches(): List<String> {
        if (!localSuggestionsEnabled()) return emptyList()
        return preferences
            .getString(RECENT_APP_SEARCHES_KEY, null)
            ?.split(RECENT_SEPARATOR)
            ?.filter(String::isNotBlank)
            .orEmpty()
    }

    fun recordRecentAppSearch(query: String) {
        if (!localSuggestionsEnabled()) return
        val cleanQuery = query.trim().replace(RECENT_SEPARATOR, " ")
        if (cleanQuery.length < 2) return
        val updated = buildList {
            add(cleanQuery)
            addAll(recentAppSearches().filterNot { it.equals(cleanQuery, ignoreCase = true) })
        }.take(MAX_RECENT_SEARCHES)
        preferences.edit { putString(RECENT_APP_SEARCHES_KEY, updated.joinToString(RECENT_SEPARATOR)) }
    }

    fun launcherSearchLearning(): List<LauncherSearchLearning> {
        if (!localSuggestionsEnabled()) return emptyList()
        return preferences
            .getString(APP_SEARCH_LEARNING_KEY, null)
            ?.split(LEARNING_ENTRY_SEPARATOR)
            ?.mapNotNull { encoded ->
                val fields = encoded.split(LEARNING_FIELD_SEPARATOR)
                if (fields.size != 4) return@mapNotNull null
                LauncherSearchLearning(
                    query = fields[0],
                    targetKey = fields[1].let { storedKey ->
                        if (storedKey.startsWith("app:") || storedKey.startsWith("shortcut:")) {
                            storedKey
                        } else {
                            "app:$storedKey"
                        }
                    },
                    selectionCount = fields[2].toIntOrNull()?.coerceAtLeast(1)
                        ?: return@mapNotNull null,
                    lastSelectedAt = fields[3].toLongOrNull() ?: return@mapNotNull null,
                )
            }
            .orEmpty()
    }

    fun recordSearchSelection(
        query: String,
        targetKey: String,
        isPrivate: Boolean,
    ) {
        if (!localSuggestionsEnabled()) return
        val normalizedQuery = query
            .replace(LEARNING_ENTRY_SEPARATOR, " ")
            .replace(LEARNING_FIELD_SEPARATOR, " ")
            .normalizedForSearch()
            .take(MAX_LEARNED_QUERY_LENGTH)
        if (normalizedQuery.length < 2 || isPrivate) return
        val now = System.currentTimeMillis()
        val existing = launcherSearchLearning()
        val previous = existing.firstOrNull {
            it.query == normalizedQuery && it.targetKey == targetKey
        }
        val updated = buildList {
            add(
                LauncherSearchLearning(
                    query = normalizedQuery,
                    targetKey = targetKey,
                    selectionCount = ((previous?.selectionCount ?: 0) + 1)
                        .coerceAtMost(MAX_LEARNED_SELECTION_COUNT),
                    lastSelectedAt = now,
                ),
            )
            addAll(existing.filterNot {
                it.query == normalizedQuery && it.targetKey == targetKey
            })
        }.sortedByDescending(LauncherSearchLearning::lastSelectedAt)
            .take(MAX_LEARNED_APP_SEARCHES)
        preferences.edit {
            putString(
                APP_SEARCH_LEARNING_KEY,
                updated.joinToString(LEARNING_ENTRY_SEPARATOR) { learning ->
                    listOf(
                        learning.query,
                        learning.targetKey,
                        learning.selectionCount.toString(),
                        learning.lastSelectedAt.toString(),
                    ).joinToString(LEARNING_FIELD_SEPARATOR)
                },
            )
        }
    }

    fun clearLocalHistory() {
        val dynamicKeys = preferences.all.keys.filter {
            it.startsWith(USAGE_COUNT_PREFIX) || it.startsWith(USAGE_LAST_PREFIX)
        }
        preferences.edit {
            dynamicKeys.forEach(::remove)
            remove(RECENT_APP_SEARCHES_KEY)
            remove(APP_SEARCH_LEARNING_KEY)
        }
    }

    private fun recordAppLaunch(app: LauncherApp) {
        val countKey = USAGE_COUNT_PREFIX + app.key
        val lastKey = USAGE_LAST_PREFIX + app.key
        preferences.edit {
            putInt(countKey, preferences.getInt(countKey, 0) + 1)
            putLong(lastKey, System.currentTimeMillis())
        }
    }

    private fun localSuggestionsEnabled(): Boolean =
        preferences.getBoolean(LOCAL_SUGGESTIONS_KEY, true)

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
        const val ONBOARDING_COMPLETE_KEY = "onboarding_complete"
        const val HOME_MODE_KEY = "home_mode"
        const val TERMINAL_BACKGROUND_COLOR_KEY = "terminal_background_color"
        const val TERMINAL_BACKGROUND_OPACITY_KEY = "terminal_background_opacity"
        const val TERMINAL_TOP_ACTION_BACKDROP_KEY = "terminal_top_action_backdrop"
        const val TERMINAL_CORNER_RADIUS_KEY = "terminal_corner_radius"
        const val PRIVATE_SPACE_EXPANDED_KEY = "private_space_expanded"
        const val DOUBLE_TAP_LOCK_KEY = "double_tap_lock_enabled"
        const val SWIPE_NOTIFICATIONS_KEY = "swipe_notifications_enabled"
        const val FROSTED_WALLPAPER_KEY = "frosted_wallpaper_enabled"
        const val NOTIFICATION_DOTS_KEY = "notification_dots_enabled"
        const val MEDIA_CONTROLS_KEY = "media_controls_enabled"
        const val LOCAL_SUGGESTIONS_KEY = "local_suggestions_enabled"
        const val SHOW_SEARCH_HINT_KEY = "show_search_hint"
        const val USAGE_COUNT_PREFIX = "usage_count:"
        const val USAGE_LAST_PREFIX = "usage_last:"
        const val RECENT_APP_SEARCHES_KEY = "recent_app_searches"
        const val APP_SEARCH_LEARNING_KEY = "app_search_learning"
        const val RECENT_SEPARATOR = "\u001F"
        const val LEARNING_ENTRY_SEPARATOR = "\u001D"
        const val LEARNING_FIELD_SEPARATOR = "\u001E"
        const val MAX_RECENT_SEARCHES = 5
        const val MAX_LEARNED_APP_SEARCHES = 48
        const val MAX_LEARNED_QUERY_LENGTH = 64
        const val MAX_LEARNED_SELECTION_COUNT = 99
        const val MIN_TERMINAL_CORNER_RADIUS = 0
        const val MAX_TERMINAL_CORNER_RADIUS = 32
        const val ICON_SIZE_PX = 144
    }
}
