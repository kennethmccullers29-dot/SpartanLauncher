package com.example.spartanlauncher

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class TileNotificationPreview(
    val title: String?,
    val text: String,
    val image: Bitmap?
)

internal object LauncherNotificationCounts {
    private data class ActiveNotification(
        val packageName: String,
        val postedAt: Long,
        val groupKey: String?,
        val summary: Boolean,
        val count: Int,
        val preview: TileNotificationPreview?
    )

    private val activeNotifications = mutableMapOf<String, ActiveNotification>()
    private val mutableCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val mutablePreviews = MutableStateFlow<Map<String, TileNotificationPreview>>(emptyMap())
    val counts = mutableCounts.asStateFlow()
    val previews = mutablePreviews.asStateFlow()

    @Synchronized
    fun replace(notifications: Array<out StatusBarNotification>, context: Context? = null) {
        activeNotifications.clear()
        notifications.forEach { add(it, context) }
        publish()
    }

    @Synchronized
    fun posted(notification: StatusBarNotification, context: Context? = null) {
        add(notification, context)
        publish()
    }

    @Synchronized
    fun removed(notification: StatusBarNotification) {
        activeNotifications.remove(notification.key)
        publish()
    }

    private fun add(sbn: StatusBarNotification, context: Context?) {
        val notification = sbn.notification
        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) {
            activeNotifications.remove(sbn.key)
            return
        }
        val summary = notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        val preview = try {
            notificationTilePreview(notification, context)
        } catch (_: RuntimeException) {
            // Malformed extras from one app must not stop the listener or other tiles.
            null
        }
        activeNotifications[sbn.key] = ActiveNotification(
            packageName = sbn.packageName,
            postedAt = sbn.postTime,
            groupKey = sbn.groupKey,
            summary = summary,
            count = if (summary) notification.number.coerceIn(1, 9999) else 1,
            preview = preview
        )
    }

    private fun publish() {
        val visible = activeNotifications.values.groupBy { it.packageName }
            .mapValues { (_, notifications) ->
                val children = notifications.filterNot { it.summary }
                // Some apps only expose a summary. Keep it until actual children are present.
                children + notifications.filter { summary ->
                    summary.summary && children.none { it.groupKey == summary.groupKey }
                }
            }
        mutableCounts.value = visible.mapValues { (_, notifications) -> notifications.sumOf { it.count } }
        mutablePreviews.value = visible.mapNotNull { (packageName, notifications) ->
            notifications.filter { it.preview != null }.maxByOrNull { it.postedAt }
                ?.preview?.let { packageName to it }
        }.toMap()
    }
}

internal data class MusicTileState(
    val title: String?,
    val artist: String?,
    val artwork: Bitmap?,
    val playing: Boolean
)

internal object LauncherMediaPlayback {
    private val mutableSessions = MutableStateFlow<Map<String, MusicTileState>>(emptyMap())
    val sessions = mutableSessions.asStateFlow()

    fun update(controllers: List<MediaController>) {
        mutableSessions.value = controllers.groupBy { it.packageName }.mapNotNull { (packageName, sessions) ->
            val controller = sessions.firstOrNull {
                it.playbackState?.state == PlaybackState.STATE_PLAYING
            } ?: sessions.firstOrNull {
                it.playbackState?.state == PlaybackState.STATE_PAUSED
            } ?: return@mapNotNull null
            val metadata = controller.metadata
            val artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
            packageName to MusicTileState(
                title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
                    ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)?.takeIf { it.isNotBlank() },
                artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }
                    ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)?.takeIf { it.isNotBlank() },
                // Media apps may resend identical artwork as a new Bitmap on every update.
                // Keep the existing image so those updates do not restart the tile's motion.
                artwork = artwork?.takeUnless { it.isRecycled }?.let { incoming ->
                    val previous = mutableSessions.value[packageName]?.artwork
                    if (previous != null && !previous.isRecycled &&
                        (previous === incoming || previous.sameAs(incoming))) previous else incoming
                },
                playing = controller.playbackState?.state == PlaybackState.STATE_PLAYING
            )
        }.toMap()
    }

    fun clear() {
        mutableSessions.value = emptyMap()
    }
}

class LauncherNotificationListener : NotificationListenerService() {
    private var mediaSessionManager: MediaSessionManager? = null
    private var activeSessionsListener: MediaSessionManager.OnActiveSessionsChangedListener? = null
    private val mediaControllerCallbacks = mutableMapOf<MediaController, MediaController.Callback>()
    private var mediaControllers: List<MediaController> = emptyList()

    override fun onListenerConnected() {
        super.onListenerConnected()
        connectedListener = this
        refreshSnapshot()
        stopMediaObservation()
        try {
            val manager = getSystemService(MediaSessionManager::class.java) ?: return
            mediaSessionManager = manager
            val listener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
                updateMediaSessions(controllers.orEmpty())
            }
            activeSessionsListener = listener
            manager.addOnActiveSessionsChangedListener(
                listener,
                ComponentName(this, LauncherNotificationListener::class.java)
            )
            updateMediaSessions(
                manager.getActiveSessions(ComponentName(this, LauncherNotificationListener::class.java))
            )
        } catch (exception: SecurityException) {
            stopMediaObservation()
            Log.w(TAG, "Media session access unavailable; notification tiles remain active", exception)
        }
    }

    private fun refreshSnapshot(): Boolean {
        return try {
            LauncherNotificationCounts.replace(activeNotifications.orEmpty(), this)
            true
        } catch (exception: SecurityException) {
            Log.w(TAG, "Notification snapshot unavailable", exception)
            false
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        LauncherNotificationCounts.posted(sbn, this)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        LauncherNotificationCounts.removed(sbn)
    }

    override fun onListenerDisconnected() {
        if (connectedListener === this) connectedListener = null
        LauncherNotificationCounts.replace(emptyArray())
        stopMediaObservation()
        super.onListenerDisconnected()
        refresh(this)
    }

    override fun onDestroy() {
        if (connectedListener === this) {
            connectedListener = null
            LauncherNotificationCounts.replace(emptyArray())
        }
        stopMediaObservation()
        super.onDestroy()
    }

    private fun stopMediaObservation() {
        activeSessionsListener?.let { listener ->
            mediaSessionManager?.removeOnActiveSessionsChangedListener(listener)
        }
        activeSessionsListener = null
        mediaSessionManager = null
        mediaControllerCallbacks.forEach { (controller, callback) -> controller.unregisterCallback(callback) }
        mediaControllerCallbacks.clear()
        mediaControllers = emptyList()
        LauncherMediaPlayback.clear()
    }

    private fun updateMediaSessions(controllers: List<MediaController>) {
        (mediaControllerCallbacks.keys - controllers.toSet()).forEach { controller ->
            mediaControllerCallbacks.remove(controller)?.let(controller::unregisterCallback)
        }
        controllers.forEach { controller ->
            if (controller !in mediaControllerCallbacks) {
                val callback = object : MediaController.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackState?) {
                        LauncherMediaPlayback.update(mediaControllers)
                    }

                    override fun onMetadataChanged(metadata: MediaMetadata?) {
                        LauncherMediaPlayback.update(mediaControllers)
                    }

                    override fun onSessionDestroyed() {
                        updateMediaSessions(mediaControllers.filterNot { it == controller })
                    }
                }
                mediaControllerCallbacks[controller] = callback
                controller.registerCallback(callback)
            }
        }
        mediaControllers = controllers
        LauncherMediaPlayback.update(controllers)
    }

    companion object {
        private const val TAG = "LauncherNotifications"
        private var connectedListener: LauncherNotificationListener? = null

        // Called on the main thread when returning home, including after an IDE rebuild.
        internal fun refresh(context: Context) {
            if (context.packageName !in NotificationManagerCompat.getEnabledListenerPackages(context)) {
                LauncherNotificationCounts.replace(emptyArray())
                LauncherMediaPlayback.clear()
                return
            }
            if (connectedListener?.refreshSnapshot() == true) return
            try {
                requestRebind(ComponentName(context, LauncherNotificationListener::class.java))
            } catch (exception: SecurityException) {
                Log.w(TAG, "Unable to reconnect notification listener", exception)
            }
        }
    }
}
