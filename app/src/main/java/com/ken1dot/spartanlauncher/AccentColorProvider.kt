package com.ken1dot.spartanlauncher

import android.content.ContentProvider
import android.content.ContentValues
import android.os.Binder
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/** Read-only bridge for apps that follow SpartanLauncher's accent color. */
class AccentColorProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        if (uri == LOCK_SETTINGS_URI) {
            val enabled = context?.getSharedPreferences("launcher", 0)
                ?.getBoolean("lock_zune_text_enabled", false) ?: false
            return MatrixCursor(arrayOf("zune_text_enabled")).apply {
                addRow(arrayOf(if (enabled) 1 else 0))
                context?.contentResolver?.let { setNotificationUri(it, LOCK_SETTINGS_URI) }
            }
        }
        if (uri == ICON_PACK_URI) {
            // Selected icon pack package ("" = default icons), for SpartanLock's notification icons.
            val pack = context?.getSharedPreferences("spartan_icon_pack", 0)?.getString("pack", "").orEmpty()
            return MatrixCursor(arrayOf("pack")).apply { addRow(arrayOf(pack)) }
        }
        if (uri != COLOR_URI) return null
        val color = context?.getSharedPreferences("launcher", 0)
            ?.getInt("launcher_accent_color", DEFAULT_ACCENT) ?: DEFAULT_ACCENT
        return MatrixCursor(arrayOf("color")).apply { addRow(arrayOf(color)) }
    }

    override fun getType(uri: Uri): String? = when (uri) {
        COLOR_URI -> "vnd.android.cursor.item/vnd.spartanlauncher.accent"
        WALLPAPER_URI -> "image/*"
        LOCK_SETTINGS_URI -> "vnd.android.cursor.item/vnd.spartanlauncher.lock-settings"
        ICON_PACK_URI -> "vnd.android.cursor.item/vnd.spartanlauncher.icon-pack"
        else -> null
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (uri != WALLPAPER_URI || mode != "r") throw FileNotFoundException()
        val caller = context?.packageManager?.getPackagesForUid(Binder.getCallingUid()).orEmpty()
        if ("com.ken1dot.spartanlock" !in caller) throw FileNotFoundException()
        val app = context ?: throw FileNotFoundException()
        val preferences = app.getSharedPreferences("launcher", 0)
        // Lock screen background chosen in Personalization > Lock screen.
        when (preferences.getString(LOCK_WALLPAPER_MODE, "start")) {
            "photo" -> File(app.filesDir, LOCK_WALLPAPER_FILE).takeIf { it.isFile }
            "bing" -> File(app.filesDir, "bing-wallpaper.jpg").takeIf { it.isFile }
            else -> null
        }?.let { return ParcelFileDescriptor.open(it, ParcelFileDescriptor.MODE_READ_ONLY) }
        if (preferences.getBoolean(BING_WALLPAPER_ENABLED, false)) {
            val image = File(app.filesDir, "bing-wallpaper.jpg")
            if (!image.isFile) throw FileNotFoundException()
            return ParcelFileDescriptor.open(image, ParcelFileDescriptor.MODE_READ_ONLY)
        }
        val selected = preferences.getString("launcher_wallpaper_uri", null)
            ?: throw FileNotFoundException()
        return app.contentResolver.openFileDescriptor(Uri.parse(selected), "r")
            ?: throw FileNotFoundException()
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    companion object {
        val COLOR_URI: Uri = Uri.parse("content://com.ken1dot.spartanlauncher.accent/color")
        val WALLPAPER_URI: Uri = Uri.parse("content://com.ken1dot.spartanlauncher.accent/wallpaper")
        val LOCK_SETTINGS_URI: Uri = Uri.parse("content://com.ken1dot.spartanlauncher.accent/lock-settings")
        val ICON_PACK_URI: Uri = Uri.parse("content://com.ken1dot.spartanlauncher.accent/icon-pack")
        private const val DEFAULT_ACCENT = 0xFF0050EF.toInt() // Cobalt.
    }
}
