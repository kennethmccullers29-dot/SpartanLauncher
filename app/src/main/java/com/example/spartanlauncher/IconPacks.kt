package com.example.spartanlauncher

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.util.concurrent.ConcurrentHashMap

internal data class IconPackInfo(val packageName: String, val label: String)

/** Standard ADW/Nova/GO style icon packs: appfilter.xml maps ComponentInfo{pkg/cls} to a drawable. */
internal object IconPacks {
    private const val PREFS = "spartan_icon_pack"
    private val intents = listOf(
        "org.adw.launcher.THEMES", "com.novalauncher.THEME", "com.gau.go.launcherex.theme",
        "com.teslacoilsw.launcher.THEME", "com.anddoes.launcher.THEME", "com.fede.launcher.THEME_ICONPACK",
        "com.dlto.atom.launcher.THEME", "ginlemon.smartlauncher.THEMES"
    )
    /** Empty = default icons. */
    var current by mutableStateOf("")
        private set
    var version by mutableStateOf(0)
        private set
    @Volatile private var filterFor = ""
    @Volatile private var filter: Map<String, String> = emptyMap()
    private val cache = ConcurrentHashMap<String, ImageBitmap>()
    private val missing = ConcurrentHashMap.newKeySet<String>()

    /** The pack icon already resolved for this activity, if the chosen pack has one. */
    fun resolved(packageName: String, activityName: String): ImageBitmap? =
        if (current.isEmpty() || version < 0) null else cache["$packageName/$activityName"]

    fun load(context: Context) {
        current = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("pack", "").orEmpty()
    }

    fun save(context: Context, pack: String) {
        if (pack == current) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("pack", pack).apply()
        current = pack
        cache.clear(); missing.clear(); filterFor = ""; filter = emptyMap()
        version++
    }

    fun installed(context: Context): List<IconPackInfo> {
        val pm = context.packageManager
        return intents.flatMap { action ->
            runCatching { pm.queryIntentActivities(Intent(action), 0) }.getOrDefault(emptyList())
        }.mapNotNull { it.activityInfo?.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != context.packageName }
            .map { IconPackInfo(it.packageName, it.loadLabel(pm).toString()) }
            .sortedBy { it.label.lowercase() }
    }

    /** True for the messaging app, which always keeps its Spartan icon. */
    fun excluded(context: Context, packageName: String): Boolean {
        val sms = runCatching { android.provider.Telephony.Sms.getDefaultSmsPackage(context) }.getOrNull()
        return packageName == sms || packageName.contains("messaging") || packageName.endsWith(".mms")
    }

    /** The pack's icon for an activity, or null to fall back to the app's own icon. */
    fun iconFor(context: Context, packageName: String, activityName: String): ImageBitmap? {
        val pack = current
        if (pack.isEmpty() || excluded(context, packageName)) return null
        val key = "$packageName/$activityName"
        cache[key]?.let { return it }
        if (key in missing) return null
        val res = runCatching { context.packageManager.getResourcesForApplication(pack) }
            .onFailure { android.util.Log.w("IconPack", "no resources for $pack", it) }.getOrNull() ?: return null
        val map = filterOf(context, pack, res)
        val name = map[key] ?: map.entries.firstOrNull { it.key.startsWith("$packageName/") }?.value
        val id = name?.let { res.getIdentifier(it, "drawable", pack) }?.takeIf { it != 0 }
        val bmp = id?.let {
            runCatching { res.getDrawable(it, null).toBitmap(144, 144, Bitmap.Config.ARGB_8888).asImageBitmap() }.getOrNull()
        }
        if (bmp == null) { missing += key; android.util.Log.d("IconPack", "miss $key name=$name id=$id"); return null }
        android.util.Log.d("IconPack", "hit $key -> $name")
        cache[key] = bmp
        return bmp
    }

    @Synchronized
    private fun filterOf(context: Context, pack: String, res: android.content.res.Resources): Map<String, String> {
        if (filterFor == pack) return filter
        val map = HashMap<String, String>()
        val parser: XmlPullParser? = runCatching {
            val p = XmlPullParserFactory.newInstance().newPullParser()
            p.setInput(res.assets.open("appfilter.xml"), "UTF-8"); p
        }.getOrNull() ?: res.getIdentifier("appfilter", "xml", pack).takeIf { it != 0 }?.let { res.getXml(it) }
        runCatching {
            parser?.let { p ->
                while (p.next() != XmlPullParser.END_DOCUMENT) {
                    if (p.eventType != XmlPullParser.START_TAG || p.name != "item") continue
                    val comp = p.getAttributeValue(null, "component") ?: continue
                    val drawable = p.getAttributeValue(null, "drawable") ?: continue
                    val inner = comp.substringAfter("ComponentInfo{", "").substringBefore("}")
                    if (inner.isEmpty()) continue
                    val pkg = inner.substringBefore("/")
                    var cls = inner.substringAfter("/", "")
                    if (cls.startsWith(".")) cls = pkg + cls
                    map.putIfAbsent("$pkg/$cls", drawable)
                }
            }
        }
        android.util.Log.d("IconPack", "parsed $pack: ${map.size} mappings")
        filterFor = pack; filter = map
        return map
    }
}