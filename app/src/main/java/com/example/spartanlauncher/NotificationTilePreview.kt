package com.example.spartanlauncher

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap

internal fun notificationTilePreview(
    notification: Notification,
    context: Context? = null
): TileNotificationPreview? {
    if (notification.visibility == Notification.VISIBILITY_SECRET) return null
    val extras = notification.extras ?: Bundle.EMPTY
    fun text(key: String) = (extras.get(key) as? CharSequence)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
    val messages = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        ?.messages.orEmpty()
    val message = messages.filter { !it.text.isNullOrBlank() }.maxByOrNull { it.timestamp }
    val title = text(Notification.EXTRA_CONVERSATION_TITLE)
        ?: message?.person?.name?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        ?: text(Notification.EXTRA_TITLE_BIG)
        ?: text(Notification.EXTRA_TITLE)
    val inbox = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        ?.mapNotNull { line -> line?.toString()?.trim()?.takeIf { it.isNotEmpty() } }
        ?.takeLast(3)?.joinToString("\n")?.takeIf { it.isNotEmpty() }
    val body = message?.text?.toString()?.trim()
        ?: text(Notification.EXTRA_BIG_TEXT)
        ?: text(Notification.EXTRA_TEXT)
        ?: inbox
        ?: notification.tickerText?.toString()?.trim()?.takeIf { it.isNotEmpty() }

    // A broken image must not prevent delivery of the text or badge.
    val image = try {
        sequenceOf(
            extras.get(Notification.EXTRA_PICTURE),
            extras.get("android.pictureIcon")
        ).firstNotNullOfOrNull { value ->
            when (value) {
                is Bitmap -> value
                is Icon -> context?.let { value.loadDrawable(it) }?.let { drawable ->
                    val width = drawable.intrinsicWidth.coerceAtLeast(1)
                    val height = drawable.intrinsicHeight.coerceAtLeast(1)
                    val scale = minOf(1f, 1280f / maxOf(width, height))
                    drawable.toBitmap(
                        (width * scale).toInt().coerceAtLeast(1),
                        (height * scale).toInt().coerceAtLeast(1)
                    )
                }
                else -> null
            }
        }?.let { bitmap ->
            val side = maxOf(bitmap.width, bitmap.height)
            if (side <= 1280) bitmap else {
                val scale = 1280f / side
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            }
        }
    } catch (_: RuntimeException) {
        null
    }
    if (title == null && body == null && image == null) return null
    return TileNotificationPreview(title, body ?: title ?: "New notification", image)
}
