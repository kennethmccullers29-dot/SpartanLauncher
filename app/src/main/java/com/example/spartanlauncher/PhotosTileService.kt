package com.example.spartanlauncher

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class PhotosTileState(
    val photos: List<ImageBitmap> = emptyList(),
    val photoIds: List<Long> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

internal fun shouldReloadPhotos(currentIds: List<Long>, nextIds: List<Long>?, hasError: Boolean): Boolean {
    if (hasError || nextIds == null) return true
    return nextIds != currentIds
}

internal suspend fun recentPhotoIds(context: Context): List<Long>? = withContext(Dispatchers.IO) {
    val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    context.contentResolver.query(
        collection,
        arrayOf(MediaStore.Images.Media._ID),
        null,
        null,
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
    )?.use { cursor ->
        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        buildList {
            while (cursor.moveToNext() && size < MAX_PHOTOS_TILE_IMAGES) add(cursor.getLong(idIndex))
        }
    }
}

internal suspend fun loadRecentPhotos(context: Context, ids: List<Long>): PhotosTileState =
    withContext(Dispatchers.IO) {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val photos = ids.mapNotNull { id ->
            val uri = ContentUris.withAppendedId(collection, id)
            try {
                decodePhoto(context, uri)?.asImageBitmap()
            } catch (_: java.io.IOException) {
                // Deleted or unreadable media should not blank the entire tile.
                null
            }
        }
        PhotosTileState(photos = photos, photoIds = ids)
    }

private fun decodePhoto(context: Context, uri: Uri): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
        return android.graphics.ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
            val scale = minOf(1f, 1024f / maxOf(info.size.width, info.size.height))
            decoder.setTargetSize((info.size.width * scale).toInt().coerceAtLeast(1),
                (info.size.height * scale).toInt().coerceAtLeast(1))
        }
    }
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val boundsStream = resolver.openInputStream(uri) ?: return null
    boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
}

private const val MAX_PHOTOS_TILE_IMAGES = 12
