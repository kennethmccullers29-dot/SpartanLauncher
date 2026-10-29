package com.example.spartanlauncher

import android.app.Notification
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NotificationTilesTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val packageName = "test.notification.tiles"

    @Before
    fun reset() = instrumentation.runOnMainSync {
        LauncherNotificationCounts.replace(emptyArray())
    }

    @After
    fun cleanUp() = reset()

    private fun notification(title: String = "Title", body: String = "Body") =
        Notification.Builder(context, "tile-tests")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)

    private fun posted(id: Int, notification: Notification, time: Long = id.toLong()) =
        StatusBarNotification(packageName, packageName, id, null, android.os.Process.myUid(), 0, 0, notification, android.os.Process.myUserHandle(), time)

    @Test
    fun blankExpandedTextFallsBackToBasicText() = instrumentation.runOnMainSync {
        val value = notification().build()
        value.extras.putCharSequence(Notification.EXTRA_BIG_TEXT, " ")
        assertEquals("Body", notificationTilePreview(value)?.text)
    }

    @Test
    fun messagingAndInboxStylesProduceReadablePreviews() = instrumentation.runOnMainSync {
        val chat = NotificationCompat.Builder(context, "tile-tests")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setStyle(
                NotificationCompat.MessagingStyle("Me")
                    .addMessage("Older message", 10L, "Alice")
                    .addMessage("Latest message", 20L, "Bob")
            ).build()
        assertEquals("Latest message", notificationTilePreview(chat)?.text)
        assertEquals("Bob", notificationTilePreview(chat)?.title)

        val inbox = Notification.Builder(context, "tile-tests")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setStyle(Notification.InboxStyle().addLine("First email").addLine("Second email"))
            .build()
        assertEquals("First email\nSecond email", notificationTilePreview(inbox)?.text)
    }

    @Test
    fun secretNotificationsKeepCountsWithoutExposingContent() = instrumentation.runOnMainSync {
        val secret = notification().setVisibility(Notification.VISIBILITY_SECRET).build()
        LauncherNotificationCounts.posted(posted(1, secret))
        assertEquals(1, LauncherNotificationCounts.counts.value[packageName])
        assertNull(LauncherNotificationCounts.previews.value[packageName])
    }

    @Test
    fun summaryIsFallbackAndDoesNotDoubleCountChildren() = instrumentation.runOnMainSync {
        val summary = posted(1, notification().setGroup("chat").setGroupSummary(true).setNumber(4).build())
        val child = posted(2, notification("Child", "Message").setGroup("chat").build())
        LauncherNotificationCounts.posted(summary)
        assertEquals(4, LauncherNotificationCounts.counts.value[packageName])
        LauncherNotificationCounts.posted(child)
        assertEquals(1, LauncherNotificationCounts.counts.value[packageName])
        assertEquals("Message", LauncherNotificationCounts.previews.value[packageName]?.text)
        LauncherNotificationCounts.removed(child)
        assertEquals(4, LauncherNotificationCounts.counts.value[packageName])
        LauncherNotificationCounts.removed(summary)
        assertNull(LauncherNotificationCounts.counts.value[packageName])
    }

    @Test
    fun senderAvatarIsNotUsedAsFullTileBackground() = instrumentation.runOnMainSync {
        val avatar = android.graphics.Bitmap.createBitmap(32, 32, android.graphics.Bitmap.Config.ARGB_8888)
        val value = notification().setLargeIcon(avatar).build()
        assertNull(notificationTilePreview(value)?.image)
        value.extras.putParcelable(Notification.EXTRA_PICTURE, avatar)
        assertEquals(avatar, notificationTilePreview(value)?.image)
    }

    @Test
    fun refreshRestoresActiveItemsAndDropsStaleItems() = instrumentation.runOnMainSync {
        LauncherNotificationCounts.posted(posted(1, notification("Old", "Old message").build()))
        val current = posted(2, notification("Current", "Current message").build())
        LauncherNotificationCounts.replace(arrayOf(current))
        assertEquals(1, LauncherNotificationCounts.counts.value[packageName])
        assertEquals("Current message", LauncherNotificationCounts.previews.value[packageName]?.text)
        LauncherNotificationCounts.removed(current)
        assertNull(LauncherNotificationCounts.counts.value[packageName])
        assertNull(LauncherNotificationCounts.previews.value[packageName])
    }
}
