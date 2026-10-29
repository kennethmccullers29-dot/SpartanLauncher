package com.ken1dot.spartanlauncher

import android.content.Context
import android.graphics.BitmapFactory
import android.util.AtomicFile
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.time.LocalDate
import kotlin.coroutines.coroutineContext

internal const val BING_WALLPAPER_ENABLED = "bing_wallpaper_enabled"
internal object BingWallpaperAccess {
    var enabled by mutableStateOf(false)
    var revision by mutableIntStateOf(0)
    var loading by mutableStateOf(false)
    var image by mutableStateOf<ImageBitmap?>(null)
    var credit by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
}

internal data class BingWallpaperInfo(val url: String, val credit: String)

internal fun parseBingWallpaper(root: JSONObject): BingWallpaperInfo {
    val item = root.getJSONArray("images").getJSONObject(0)
    if (!item.optBoolean("wp", true)) throw IOException("Today's image is not available as wallpaper")
    val uri = URI("https://www.bing.com").resolve(item.getString("url"))
    if (uri.scheme != "https" || uri.host != "www.bing.com" || uri.userInfo != null)
        throw IOException("Invalid wallpaper address")
    return BingWallpaperInfo(uri.toString(), item.optString("copyright", "Bing image of the day"))
}

private fun bingBytes(address: String, limit: Int): ByteArray {
    val connection = URL(address).openConnection() as HttpURLConnection
    connection.connectTimeout = 15_000
    connection.readTimeout = 15_000
    try {
        if (connection.responseCode != 200) throw IOException("Bing is temporarily unavailable")
        return connection.inputStream.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val size = stream.read(buffer)
                if (size < 0) break
                if (output.size() + size > limit) throw IOException("Wallpaper response is too large")
                output.write(buffer, 0, size)
            }
            output.toByteArray()
        }
    } finally { connection.disconnect() }
}

private fun decodeBingWallpaper(bytes: ByteArray): ImageBitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Invalid wallpaper image")
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 3840) sample *= 2
    return (BitmapFactory.decodeByteArray(bytes, 0, bytes.size,
        BitmapFactory.Options().apply { inSampleSize = sample }) ?: throw IOException("Could not decode wallpaper"))
        .asImageBitmap()
}

internal suspend fun cachedBingWallpaper(context: Context): ImageBitmap? = withContext(Dispatchers.IO) {
    try { decodeBingWallpaper(AtomicFile(File(context.filesDir, "bing-wallpaper.jpg")).readFully()) }
    catch (_: IOException) { null }
}

internal suspend fun loadBingWallpaper(context: Context, force: Boolean): Pair<ImageBitmap, String> = withContext(Dispatchers.IO) {
    val prefs = context.getSharedPreferences("launcher", Context.MODE_PRIVATE)
    val today = LocalDate.now().toString()
    if (!force && prefs.getString("bing_wallpaper_day", null) == today) {
        cachedBingWallpaper(context)?.let { return@withContext it to prefs.getString("bing_wallpaper_credit", "").orEmpty() }
    }
    val info = parseBingWallpaper(JSONObject(String(bingBytes(
        "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=en-US", 128 * 1024), Charsets.UTF_8)))
    val bytes = bingBytes(info.url, 8 * 1024 * 1024)
    val image = decodeBingWallpaper(bytes)
    coroutineContext.ensureActive()
    val file = AtomicFile(File(context.filesDir, "bing-wallpaper.jpg"))
    val output = file.startWrite()
    try { output.write(bytes); file.finishWrite(output) }
    catch (exception: IOException) { file.failWrite(output); throw exception }
    prefs.edit().putString("bing_wallpaper_day", today).putString("bing_wallpaper_credit", info.credit).apply()
    image to info.credit
}
