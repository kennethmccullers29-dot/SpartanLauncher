package com.ken1dot.spartanlauncher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal const val LOCK_WALLPAPER_MODE = "lock_wallpaper_mode" // start | photo | bing
internal const val LOCK_WALLPAPER_FILE = "lock-wallpaper.jpg"

private fun lockFile(context: Context) = File(context.filesDir, LOCK_WALLPAPER_FILE)

private fun loadPreview(context: Context, mode: String): ImageBitmap? = try {
    val file = when (mode) {
        "photo" -> lockFile(context)
        "bing" -> File(context.filesDir, "bing-wallpaper.jpg")
        else -> null
    }
    if (file != null && file.isFile) ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { d, info, _ ->
        d.setTargetSampleSize(maxOf(1, maxOf(info.size.width, info.size.height) / 600))
    }.asImageBitmap()
    else context.getSharedPreferences("launcher", 0).getString("launcher_wallpaper_uri", null)?.let { uri ->
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, Uri.parse(uri))) { d, info, _ ->
            d.setTargetSampleSize(maxOf(1, maxOf(info.size.width, info.size.height) / 600))
        }.asImageBitmap()
    }
} catch (_: Exception) { null }

@Composable
internal fun LockScreenSettings() {
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("launcher", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(preferences.getBoolean("lock_zune_text_enabled", false)) }
    var mode by remember { mutableStateOf(preferences.getString(LOCK_WALLPAPER_MODE, "start") ?: "start") }
    var revision by remember { mutableIntStateOf(0) }
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }
    var cropping by remember { mutableStateOf<Bitmap?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    fun apply(newMode: String) {
        mode = newMode
        preferences.edit().putString(LOCK_WALLPAPER_MODE, newMode).apply()
        context.contentResolver.notifyChange(AccentColorProvider.WALLPAPER_URI, null)
        context.contentResolver.notifyChange(AccentColorProvider.LOCK_SETTINGS_URI, null)
        revision++
    }
    LaunchedEffect(mode, revision) {
        if (mode == "bing" && !File(context.filesDir, "bing-wallpaper.jpg").isFile) {
            status = "Getting today's Bing image…"
            status = runCatching { loadBingWallpaper(context, false); null }.getOrElse { "Bing is unavailable right now" }
        }
        preview = withContext(Dispatchers.IO) { loadPreview(context, mode) }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            cropping = withContext(Dispatchers.IO) {
                runCatching {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { d, info, _ ->
                        d.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        d.setTargetSampleSize(maxOf(1, maxOf(info.size.width, info.size.height) / 3000))
                    }
                }.getOrNull()
            }
            if (cropping == null) status = "Couldn't open that photo"
        }
    }

    Text("Background", color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
    Row(verticalAlignment = Alignment.Top) {
        // Phone-shaped preview of the current lock background.
        Box(Modifier.width(92.dp).aspectRatio(9f / 19.5f).background(Color(0xFF1F1F1F)).border(1.dp, Color(0xFF444444))) {
            preview?.let { Image(it, "Lock screen background preview", Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            Text(java.text.SimpleDateFormat("h:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                Modifier.align(Alignment.BottomStart).padding(6.dp, 0.dp, 0.dp, 18.dp), color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf("start" to "Same as Start", "photo" to "Photo", "bing" to "Bing daily").forEach { (key, label) ->
                Row(Modifier.fillMaxWidth().metroTilt {
                    if (key == "photo" && !lockFile(context).isFile) picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    else apply(key)
                }.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    MetroRadio(mode == key, null)
                    Spacer(Modifier.width(8.dp))
                    Text(label, color = Color.White, fontFamily = MetroRegular, fontSize = 17.sp)
                }
            }
        }
    }
    // W10M-style text buttons (gray block buttons like "Browse").
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.background(Color(0xFF333333)).metroTilt {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }.padding(horizontal = 18.dp, vertical = 8.dp)) { Text("Browse", color = Color.White, fontFamily = MetroRegular, fontSize = 15.sp) }
    }
    status?.let { Text(it, color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 13.sp) }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Zune animated text", color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
            Text("Moving artist and song text in SpartanLock while music plays",
                color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
        }
        MetroToggle(checked = enabled, onCheckedChange = {
            enabled = it
            preferences.edit().putBoolean("lock_zune_text_enabled", it).apply()
            context.contentResolver.notifyChange(AccentColorProvider.LOCK_SETTINGS_URI, null)
        })
    }

    cropping?.let { source ->
        LockCropDialog(source, onCancel = { cropping = null }) { cropped ->
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        val tmp = File(context.filesDir, "$LOCK_WALLPAPER_FILE.tmp")
                        tmp.outputStream().use { cropped.compress(Bitmap.CompressFormat.JPEG, 92, it) }
                        require(tmp.renameTo(lockFile(context)))
                    }.isSuccess
                }
                cropping = null
                if (ok) { status = null; apply("photo") } else status = "Couldn't save the photo"
            }
        }
    }
}

/** W10M "crop" page: drag and pinch the photo inside the phone frame, then accept or cancel. */
@Composable
private fun LockCropDialog(source: Bitmap, onCancel: () -> Unit, onDone: (Bitmap) -> Unit) {
    val image = remember(source) { source.asImageBitmap() }
    var frame by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Column(Modifier.fillMaxSize().background(Color.Black)) {
            Text("CROP", Modifier.padding(start = 16.dp, top = 40.dp, bottom = 10.dp), color = Color.White, fontFamily = MetroRegular, fontSize = 15.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().onSizeChanged { frame = it }
                .pointerInput(source) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f); offset += pan
                    }
                }) {
                // Base fill: the photo covers the frame (Crop), then user scale/offset on top.
                val base = if (frame.width == 0) 1f else maxOf(frame.width.toFloat() / source.width, frame.height.toFloat() / source.height)
                val maxX = ((source.width * base * scale) - frame.width) / 2f
                val maxY = ((source.height * base * scale) - frame.height) / 2f
                val ox = offset.x.coerceIn(-maxX.coerceAtLeast(0f), maxX.coerceAtLeast(0f))
                val oy = offset.y.coerceIn(-maxY.coerceAtLeast(0f), maxY.coerceAtLeast(0f))
                Image(image, "Photo to crop", Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = ox; translationY = oy },
                    contentScale = ContentScale.Crop)
                // Lock clock guide.
                Text("12:00", Modifier.align(Alignment.BottomStart).padding(start = 22.dp, bottom = 90.dp), color = Color.White.copy(alpha = .8f),
                    fontFamily = MetroRegular, fontSize = 64.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Light)
                Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xFF1F1F1F)).padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    AppBarButton("✓", "accept") {
                        if (frame.width > 0) {
                            // Map the visible frame back into source pixels.
                            val s = base * scale
                            val visW = frame.width / s; val visH = frame.height / s
                            val cx = source.width / 2f - ox / s; val cy = source.height / 2f - oy / s
                            val left = (cx - visW / 2).toInt().coerceIn(0, source.width - 1)
                            val top = (cy - visH / 2).toInt().coerceIn(0, source.height - 1)
                            val w = visW.toInt().coerceAtMost(source.width - left).coerceAtLeast(1)
                            val h = visH.toInt().coerceAtMost(source.height - top).coerceAtLeast(1)
                            val crop = Bitmap.createBitmap(source, left, top, w, h)
                            val out = if (crop.height > 2400) Bitmap.createScaledBitmap(crop, crop.width * 2400 / crop.height, 2400, true) else crop
                            onDone(out)
                        }
                    }
                    AppBarButton("✕", "cancel", onCancel)
                }
            }
        }
    }
}

@Composable
private fun AppBarButton(glyph: String, label: String, onClick: () -> Unit) {
    Column(Modifier.metroTilt(onClick = onClick).padding(horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(40.dp).border(2.dp, Color.White, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
            Text(glyph, color = Color.White, fontSize = 18.sp)
        }
        Text(label, color = Color.White, fontFamily = MetroRegular, fontSize = 11.sp)
    }
}
// LOCKWALL1

private val LOCK_SETTINGS = android.net.Uri.parse("content://com.example.spartanlock.settings")
private fun lockCall(context: Context, method: String, arg: String? = null, extras: android.os.Bundle? = null): android.os.Bundle? =
    runCatching { context.contentResolver.call(LOCK_SETTINGS, method, arg, extras) }.getOrNull()

/** SpartanLock's own settings (theme, Glance, Windows Hello name, PIN), via SpartanLock's provider. */
@Composable
internal fun SpartanLockSettings() {
    val context = LocalContext.current
    var info by remember { mutableStateOf(lockCall(context, "get")) }
    if (info == null) {
        Text("Install or update SpartanLock to change these settings.", color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
        return
    }
    val data = info!!
    val ids = data.getStringArray("themeIds").orEmpty()
    val titles = data.getStringArray("themeTitles").orEmpty()
    Text("Clock style", color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
    ids.forEachIndexed { i, id ->
        Row(Modifier.fillMaxWidth().metroTilt { info = lockCall(context, "setTheme", id) }.padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically) {
            MetroRadio(data.getString("theme") == id, null)
            Spacer(Modifier.width(8.dp))
            Text(titles.getOrElse(i) { id }, color = Color.White, fontFamily = MetroRegular, fontSize = 17.sp)
        }
    }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Glance screen", color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp)
            Text("Show the time and notifications on a dim black screen after the screen turns off",
                color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
        }
        MetroToggle(checked = data.getBoolean("glance"), onCheckedChange = { info = lockCall(context, "setGlance", it.toString()) })
    }
    Text("Windows Hello", color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
    Text("Greeting name", color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
    var name by remember { mutableStateOf(data.getString("name").orEmpty()) }
    MetroTextBox(name, { name = it.take(40); lockCall(context, "setName", name) }, password = false)

    Text(if (data.getBoolean("hasPin")) "PIN" else "PIN (not set)", color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
    Text("Asked when you swipe up. Fingerprint or face unlock appears as Windows Hello.", color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    if (data.getBoolean("hasPin")) {
        Text("Current PIN", color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
        MetroTextBox(old, { old = it.filter(Char::isDigit).take(16) }, password = true)
    }
    Text("New PIN (4+ digits)", color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
    MetroTextBox(new, { new = it.filter(Char::isDigit).take(16) }, password = true)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetroBlockButton(if (data.getBoolean("hasPin")) "Change" else "Set PIN") {
            val r = lockCall(context, "setPin", extras = android.os.Bundle().apply { putString("pin", new); putString("old", old) })
            msg = if (r?.getBoolean("ok") == true) "PIN saved" else "Check the current PIN and use 4 or more digits"
            if (r != null) info = r; old = ""; new = ""
        }
        if (data.getBoolean("hasPin")) MetroBlockButton("Remove") {
            val r = lockCall(context, "clearPin", extras = android.os.Bundle().apply { putString("old", old) })
            msg = if (r?.getBoolean("ok") == true) "PIN removed" else "That PIN is incorrect"
            if (r != null) info = r; old = ""; new = ""
        }
    }
    msg?.let { Text(it, color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 13.sp) }
    Text("Accessibility", color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
    Text("SpartanLock needs its accessibility service on to show over the lock screen.", color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
    MetroBlockButton("Accessibility settings") {
        runCatching { context.startActivity(android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}

@Composable
private fun MetroBlockButton(label: String, onClick: () -> Unit) {
    Box(Modifier.background(Color(0xFF333333)).metroTilt(onClick = onClick).padding(horizontal = 18.dp, vertical = 8.dp)) {
        Text(label, color = Color.White, fontFamily = MetroRegular, fontSize = 15.sp)
    }
}

@Composable
private fun MetroTextBox(value: String, onChange: (String) -> Unit, password: Boolean) {
    androidx.compose.foundation.text.BasicTextField(value, onChange, singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontFamily = MetroRegular, fontSize = 17.sp),
        visualTransformation = if (password) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType =
            if (password) androidx.compose.ui.text.input.KeyboardType.NumberPassword else androidx.compose.ui.text.input.KeyboardType.Text),
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 8.dp))
}