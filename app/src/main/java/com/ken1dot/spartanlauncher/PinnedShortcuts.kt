package com.ken1dot.spartanlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Shortcuts other apps asked us to pin (e.g. a contact from SpartanPeople).
 * They join Start as synthetic [LauncherApp]s under [PACKAGE] so the whole
 * tile system (order, sizes, edit mode) treats them like any other tile.
 */
internal data class PinnedShortcut(
    /** Full pinKey: "pinned.shortcut/<source package>/<shortcut id>". */
    val key: String,
    val label: String,
    val intentUri: String,
    val iconFile: String,
    /** Optional tile back-face details, if the pinning app supplied them. */
    val phone: String = "",
    val email: String = "",
)

/** Source package and shortcut id inside a "pinned.shortcut/<package>/<id>" key. */
internal fun pinnedShortcutSource(key: String): Pair<String, String>? {
    if (!key.startsWith("${PinnedShortcutStore.PACKAGE}/")) return null
    val rest = key.removePrefix("${PinnedShortcutStore.PACKAGE}/")
    val sourcePackage = rest.substringBefore('/', "")
    val shortcutId = rest.substringAfter('/', "")
    return if (sourcePackage.isNotEmpty() && shortcutId.isNotEmpty()) sourcePackage to shortcutId else null
}

internal data class ShortcutPinMerge(val pins: Set<String>, val order: List<String>)

/** Append shortcut keys. Leaves every pin already in [current] on Start. */
internal fun mergeShortcutPins(current: ShortcutPinMerge, shortcutKeys: Collection<String>): ShortcutPinMerge {
    if (shortcutKeys.all { it in current.pins && it in current.order }) return current
    return ShortcutPinMerge(
        pins = current.pins + shortcutKeys,
        order = (current.order + shortcutKeys).distinct(),
    )
}

internal object PinnedShortcutStore {
    const val PACKAGE = "pinned.shortcut"
    private const val PREFS_KEY = "pinned_shortcuts_json"
    private const val ICON_DIR = "pinned-shortcuts"

    /** Saved icon edge in px: large enough to stay sharp on a wide tile. */
    private const val ICON_SIZE = 512

    fun load(context: Context): List<PinnedShortcut> = read(context) ?: emptyList()

    /** Null means the saved list is corrupt. Callers must not replace it. */
    private fun read(context: Context): List<PinnedShortcut>? {
        val raw = context.getSharedPreferences("launcher", Context.MODE_PRIVATE)
            .getString(PREFS_KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                PinnedShortcut(
                    key = item.getString("key"),
                    label = item.getString("label"),
                    intentUri = item.getString("intent"),
                    iconFile = item.getString("icon"),
                    phone = item.optString("phone"),
                    email = item.optString("email"),
                )
            }
        }.getOrNull()
    }

    fun keys(context: Context): Set<String> = load(context).mapTo(mutableSetOf()) { it.key }

    fun intentFor(context: Context, key: String): Intent? =
        load(context).firstOrNull { it.key == key }?.let { shortcut ->
            runCatching { Intent.parseUri(shortcut.intentUri, Intent.URI_INTENT_SCHEME) }.getOrNull()
        }

    /**
     * Launch a pinned shortcut. Android strips the intent from the ShortcutInfo
     * handed to the launcher, so ask the system to fire it by package/id; the
     * stored intent is only a fallback for shortcuts that arrived with one.
     */
    fun launch(context: Context, key: String, options: android.os.Bundle? = null): Boolean {
        pinnedShortcutSource(key)?.let { (sourcePackage, shortcutId) ->
            val launched = runCatching {
                context.getSystemService(LauncherApps::class.java)
                    ?.startShortcut(sourcePackage, shortcutId, null, options, android.os.Process.myUserHandle())
            }.isSuccess
            if (launched) return true
        }
        return intentFor(context, key)?.let { intent ->
            runCatching { context.startActivity(intent, options) }.isSuccess
        } ?: false
    }

    /** Phone/email for the tile's back face, if the pinning app supplied them. */
    fun detailsFor(context: Context, key: String): Pair<String, String> =
        load(context).firstOrNull { it.key == key }?.let { it.phone to it.email } ?: ("" to "")

    fun remove(context: Context, key: String) {
        val existing = read(context) ?: return
        existing.firstOrNull { it.key == key }?.let { File(it.iconFile).delete() }
        persist(context, existing.filterNot { it.key == key })
    }

    /** Synthetic Start tiles for every stored shortcut. Call off the main thread. */
    fun loadApps(context: Context): List<LauncherApp> = load(context).map { shortcut ->
        val stored = runCatching { BitmapFactory.decodeFile(shortcut.iconFile) }.getOrNull()
        // Icons saved by older builds are only 192px and blur on larger tiles.
        // Re-fetch once at full size so existing pins sharpen without re-pinning.
        val bitmap = (if (stored == null || stored.width < ICON_SIZE) refreshIcon(context, shortcut) else null)
            ?: stored
            ?: Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
        LauncherApp(
            label = shortcut.label,
            packageName = PACKAGE,
            activityName = shortcut.key.removePrefix("$PACKAGE/"),
            icon = bitmap.asImageBitmap(),
            isDefaultDialer = false,
        )
    }

    /** Fetch the shortcut's icon again at tile resolution and overwrite the saved file. */
    private fun refreshIcon(context: Context, shortcut: PinnedShortcut): Bitmap? = runCatching {
        val (sourcePackage, shortcutId) = pinnedShortcutSource(shortcut.key) ?: return null
        val launcherApps = context.getSystemService(LauncherApps::class.java) ?: return null
        val query = LauncherApps.ShortcutQuery()
            .setPackage(sourcePackage)
            .setShortcutIds(listOf(shortcutId))
            .setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
            )
        val info = launcherApps.getShortcuts(query, android.os.Process.myUserHandle())
            ?.firstOrNull() ?: return null
        val bitmap = launcherApps.getShortcutIconDrawable(info, context.resources.displayMetrics.densityDpi)
            ?.toBitmap(ICON_SIZE, ICON_SIZE) ?: return null
        FileOutputStream(shortcut.iconFile).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        bitmap
    }.getOrNull()

    fun save(context: Context, info: ShortcutInfo) {
        val existing = read(context) ?: return
        val key = "$PACKAGE/${info.`package`}/${info.id}"
        val previousIcon = existing.firstOrNull { it.key == key }?.iconFile
        val dir = File(context.filesDir, ICON_DIR).apply { mkdirs() }
        val iconFile = File(dir, java.net.URLEncoder.encode(key, "UTF-8") + ".png")
        runCatching {
            val drawable = context.getSystemService(LauncherApps::class.java)
                ?.getShortcutIconDrawable(info, context.resources.displayMetrics.densityDpi)
            val bitmap = drawable?.toBitmap(ICON_SIZE, ICON_SIZE)
            if (bitmap != null) {
                FileOutputStream(iconFile).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            }
        }
        val intent = runCatching { info.intents?.firstOrNull() }.getOrNull() ?: info.intent
        val extras = info.extras
        val shortcut = PinnedShortcut(
            key = key,
            label = info.shortLabel?.toString()?.ifBlank { null } ?: info.longLabel?.toString() ?: "Shortcut",
            intentUri = intent?.toUri(Intent.URI_INTENT_SCHEME) ?: "",
            iconFile = iconFile.absolutePath,
            phone = extras?.getString("contact_phone").orEmpty().ifBlank {
                intent?.getStringExtra("contact_phone").orEmpty()
            },
            email = extras?.getString("contact_email").orEmpty().ifBlank {
                intent?.getStringExtra("contact_email").orEmpty()
            },
        )
        persist(context, existing.filterNot { it.key == key } + shortcut)
        if (previousIcon != null && previousIcon != iconFile.absolutePath && iconFile.isFile) {
            File(previousIcon).delete()
        }
    }

    private fun persist(context: Context, shortcuts: List<PinnedShortcut>) {
        val array = JSONArray()
        shortcuts.forEach { shortcut ->
            array.put(
                JSONObject()
                    .put("key", shortcut.key)
                    .put("label", shortcut.label)
                    .put("intent", shortcut.intentUri)
                    .put("icon", shortcut.iconFile)
                    .put("phone", shortcut.phone)
                    .put("email", shortcut.email),
            )
        }
        context.getSharedPreferences("launcher", Context.MODE_PRIVATE)
            .edit().putString(PREFS_KEY, array.toString()).commit()
    }
}

/**
 * System entry point for pin requests: apps call ShortcutManager.requestPinShortcut,
 * and Android offers the request to the default launcher via this activity.
 */
class ConfirmPinShortcutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val request = runCatching {
            getSystemService(LauncherApps::class.java)?.getPinItemRequest(intent)
        }.getOrNull()
        if (request != null && request.isValid &&
            request.requestType == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT
        ) {
            request.shortcutInfo?.let { info ->
                runCatching { request.accept() }
                PinnedShortcutStore.save(this, info)
            }
        }
        finish()
    }
}
