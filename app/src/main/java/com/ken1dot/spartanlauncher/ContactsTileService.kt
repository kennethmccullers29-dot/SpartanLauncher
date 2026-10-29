package com.ken1dot.spartanlauncher

import android.content.Context
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class PeopleTileContact(
    val id: Long,
    val name: String,
    val photo: ImageBitmap?
)

internal data class PeopleTileState(
    val contacts: List<PeopleTileContact> = emptyList(),
    val permissionGranted: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

internal suspend fun loadPeopleContacts(context: Context): PeopleTileState =
    withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val contacts = mutableListOf<PeopleTileContact>()
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )
        resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.Contacts.STARRED} DESC, ${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            while (cursor.moveToNext() && contacts.size < MAX_TILE_CONTACTS) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn).orEmpty()
                if (name.isBlank()) continue
                val photoUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id.toString())
                val photo = ContactsContract.Contacts.openContactPhotoInputStream(resolver, photoUri, true)
                    ?.use { stream -> BitmapFactory.decodeStream(stream)?.let { b ->
                        // Cache a small square thumbnail: cheap to draw, enough for a 2x2 spotlight.
                        val side = minOf(b.width, b.height)
                        val sq = android.graphics.Bitmap.createBitmap(b, (b.width - side) / 2, (b.height - side) / 2, side, side)
                        (if (side > 256) android.graphics.Bitmap.createScaledBitmap(sq, 256, 256, true) else sq).asImageBitmap()
                    } }
                contacts += PeopleTileContact(id, name, photo)
            }
        }
        PeopleTileState(contacts = contacts, permissionGranted = true)
    }

private const val MAX_TILE_CONTACTS = 60
