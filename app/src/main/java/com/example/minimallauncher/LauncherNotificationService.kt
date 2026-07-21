package com.example.minimallauncher

import android.app.Notification
import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NowPlaying(
    val packageName: String,
    val title: String,
    val artist: String?,
    val isPlaying: Boolean,
    val canSkipPrevious: Boolean,
    val canSkipNext: Boolean,
)

data class LauncherNotificationState(
    val countsByPackage: Map<String, Int> = emptyMap(),
    val nowPlaying: NowPlaying? = null,
)

enum class MediaAction { PREVIOUS, PLAY_PAUSE, NEXT }

class LauncherNotificationService : NotificationListenerService() {
    private lateinit var mediaSessionManager: MediaSessionManager
    private var mediaController: MediaController? = null
    private var notificationCounts = emptyMap<String, Int>()

    private val mediaCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publishState()
        override fun onPlaybackStateChanged(state: PlaybackState?) = publishState()
        override fun onSessionDestroyed() = refreshMediaSessions()
    }

    private val sessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            selectMediaController(controllers.orEmpty())
        }

    override fun onCreate() {
        super.onCreate()
        mediaSessionManager = getSystemService(MediaSessionManager::class.java)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeService = this
        runCatching {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                sessionsChangedListener,
                ComponentName(this, LauncherNotificationService::class.java),
            )
        }
        refreshNotifications()
        refreshMediaSessions()
    }

    override fun onListenerDisconnected() {
        disconnect()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        refreshNotifications()
        refreshMediaSessions()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        refreshNotifications()
        refreshMediaSessions()
    }

    private fun refreshNotifications() {
        notificationCounts = runCatching {
            activeNotifications
                .asSequence()
                .filterNot { it.packageName == packageName }
                .filterNot { it.notification.category == Notification.CATEGORY_TRANSPORT }
                .filterNot {
                    it.notification.flags.and(Notification.FLAG_GROUP_SUMMARY) != 0
                }
                .filter(StatusBarNotification::isClearable)
                .groupingBy(StatusBarNotification::getPackageName)
                .eachCount()
        }.getOrDefault(emptyMap())
        publishState()
    }

    private fun refreshMediaSessions() {
        val controllers = runCatching {
            mediaSessionManager.getActiveSessions(
                ComponentName(this, LauncherNotificationService::class.java),
            )
        }.getOrDefault(emptyList())
        selectMediaController(controllers)
    }

    private fun selectMediaController(controllers: List<MediaController>) {
        val next = controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull()
        if (next?.sessionToken == mediaController?.sessionToken) {
            publishState()
            return
        }
        mediaController?.unregisterCallback(mediaCallback)
        mediaController = next
        next?.registerCallback(mediaCallback)
        publishState()
    }

    private fun publishState() {
        val controller = mediaController
        val metadata = controller?.metadata
        val playbackState = controller?.playbackState
        val title = metadata?.getText(MediaMetadata.METADATA_KEY_TITLE)?.toString()
            ?: metadata?.description?.title?.toString()
        val artist = metadata?.getText(MediaMetadata.METADATA_KEY_ARTIST)?.toString()
            ?: metadata?.description?.subtitle?.toString()
        val actions = playbackState?.actions ?: 0L
        mutableState.value = LauncherNotificationState(
            countsByPackage = notificationCounts,
            nowPlaying = title?.takeIf(String::isNotBlank)?.let {
                NowPlaying(
                    packageName = controller?.packageName.orEmpty(),
                    title = it,
                    artist = artist?.takeIf(String::isNotBlank),
                    isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING,
                    canSkipPrevious = actions.and(PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L,
                    canSkipNext = actions.and(PlaybackState.ACTION_SKIP_TO_NEXT) != 0L,
                )
            },
        )
    }

    private fun perform(action: MediaAction) {
        val controller = mediaController ?: return
        when (action) {
            MediaAction.PREVIOUS -> controller.transportControls.skipToPrevious()
            MediaAction.PLAY_PAUSE -> if (
                controller.playbackState?.state == PlaybackState.STATE_PLAYING
            ) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
            MediaAction.NEXT -> controller.transportControls.skipToNext()
        }
    }

    private fun disconnect() {
        runCatching {
            mediaSessionManager.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        }
        mediaController?.unregisterCallback(mediaCallback)
        mediaController = null
        if (activeService === this) activeService = null
        mutableState.value = LauncherNotificationState()
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    companion object {
        private val mutableState = MutableStateFlow(LauncherNotificationState())
        val state: StateFlow<LauncherNotificationState> = mutableState.asStateFlow()
        @Volatile private var activeService: LauncherNotificationService? = null

        fun performMediaAction(action: MediaAction) {
            activeService?.perform(action)
        }
    }
}
