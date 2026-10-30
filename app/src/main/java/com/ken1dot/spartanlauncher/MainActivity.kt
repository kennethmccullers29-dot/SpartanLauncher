package com.ken1dot.spartanlauncher

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.composed
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.metrocompose.MetroTheme
import com.metrocompose.Tile
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.filled.BuildingShop
import fluent.ui.system.icons.filled.AppsList
import fluent.ui.system.icons.filled.Call
import fluent.ui.system.icons.filled.CalendarLtr
import fluent.ui.system.icons.filled.Camera
import fluent.ui.system.icons.filled.Chat
import fluent.ui.system.icons.filled.Clock
import fluent.ui.system.icons.filled.Folder
import fluent.ui.system.icons.filled.Globe
import fluent.ui.system.icons.filled.Image
import fluent.ui.system.icons.filled.Mail
import fluent.ui.system.icons.filled.Map
import fluent.ui.system.icons.filled.WeatherSunny
import fluent.ui.system.icons.filled.People
import fluent.ui.system.icons.regular.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

internal data class LauncherApp(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: androidx.compose.ui.graphics.ImageBitmap,
    val isDefaultDialer: Boolean,
    val canUninstall: Boolean = false
)

private data class LoadedApps(val apps: List<LauncherApp>, val updatedAt: Map<String, Long>)

private val DefaultAccent = Color(0xFF0050EF) // Cobalt, from the Windows Phone palette.
private const val START_GRID_COLUMNS = 6
private const val METRO_EXIT_DURATION = 330

/**
 * Shared Start sheet timeline for per-tile motion (0 entering, 1 rest, 2
 * leaving) plus the tile that was tapped, so each tile can run its own
 * staggered W10M turnstile: on launch tiles swing out top row first with
 * the tapped tile last; on return they swing in bottom row first.
 */
internal object MetroSheet {
    var progress: () -> Float = { 1f }
    var tappedKey by mutableStateOf<String?>(null)
    var exiting = false
    var tappedCenter = Offset.Zero
    // Glass tiles freeze their live blur into a bitmap from finger-down
    // through the dive and return, so the GPU blur isn't redone per frame.
    var pressing by mutableStateOf(false)
    var animating by mutableStateOf(false)
    val frozen: Boolean get() = pressing || animating
    // W10M unlock: 0 -> 1 while Start's tiles turnstile in after SpartanLock goes away.
    val unlock = Animatable(1f)
}
private val MetroEnterEasing = CubicBezierEasing(0.1f, 0.9f, 0.2f, 1f)
// Dive zoom: eases in gently, then accelerates into the tile at the end.
private val DiveZoomEasing = CubicBezierEasing(0.3f, 0.15f, 0.7f, 0.75f)
private val MetroExitEasing = CubicBezierEasing(0.55f, 0f, 0.9f, 0.4f)

/**
 * Opens an app. The rect is the tapped tile/row in window coordinates when
 * known, so the launched app can grow out of it.
 */
private typealias LaunchHandler = (LauncherApp, android.graphics.Rect?) -> Unit

// One interruptible timeline: 0 = entering, 1 = resting, 2 = leaving.
@Composable
private fun rememberMetroMotion(
    isExiting: Boolean,
    ready: Boolean = true,
    staggerMillis: Int = 0,
    entranceDelayMillis: Int = 0
): Animatable<Float, androidx.compose.animation.core.AnimationVector1D> {
    val motion = remember { Animatable(0f) }
    val view = androidx.compose.ui.platform.LocalView.current
    LaunchedEffect(isExiting, ready) {
        // Returning home, onResume fires while Samsung's window slide still
        // covers us — and animation time runs anyway, so the entrance used to
        // finish unseen before our window was ever presented. Wait until the
        // window actually holds focus (the visible, interactive one), then
        // play. The timeout keeps this from ever hanging the tiles.
        suspend fun awaitVisible() {
            if (entranceDelayMillis <= 0) return
            kotlinx.coroutines.withTimeoutOrNull(900) {
                while (!view.hasWindowFocus()) delay(30)
            }
            delay(entranceDelayMillis.toLong())
        }
        if (!ready) {
            // No content yet (apps still loading): wait invisibly so the
            // entrance plays when the tiles actually arrive, instead of
            // finishing behind the loading indicator.
            motion.snapTo(0f)
        } else if (isExiting) {
            motion.animateTo(
                2f,
                tween(METRO_EXIT_DURATION, staggerMillis, LinearEasing)
            )
        } else if (motion.value > 1f) {
            // Coming back from an app: the sheet rises back to rest. This runs
            // on whole pages only, so it stays smooth.
            // W10M return: Start comes back from part-way into the dive
            // (~1.13x, faint) and settles, decelerating.
            // Start straight away (no focus wait): we're already pre-set
            // to the zoomed, black state, so nothing flashes at rest.
            motion.snapTo(1.35f)
            // ...but One UI only presents our window after its own app
            // slide, so hold (black, zoomed) until we're actually shown.
            kotlinx.coroutines.withTimeoutOrNull(700) {
                while (!view.hasWindowFocus()) delay(16)
            }
            motion.animateTo(1f, tween(420, staggerMillis, MetroEnterEasing))
        } else if (motion.value < 1f) {
            motion.snapTo(0f)
            awaitVisible()
            motion.animateTo(1f, tween(170, staggerMillis, LinearEasing))
        }
    }
    return motion
}


/**
 * The Windows 10 Mobile app launch: the whole tile grid dives in toward the
 * tapped tile and fades out ("the tiles sort of zoom in and everything fades
 * nicely", build 10149 tour, 1:33). The dive tips in 3D around the tapped
 * tile — the sheet hinges toward it under perspective instead of zooming
 * flat — and returning replays the same motion in reverse, un-tipping back
 * to rest around the tile you launched. Apply to a page root only, with
 * [pivot] at the tapped tile's center when known.
 */
private fun Modifier.metroSwivel(
    pivot: Offset = Offset(0.5f, 0.5f),
    tilesAnimate: Boolean = false,
    exiting: (() -> Boolean)? = null,
    progress: () -> Float
): Modifier = graphicsLayer {
    val value = progress()
    if (exiting != null && value > 1f && !exiting()) {
        // Returning from an app (All apps): the old path replayed the dive
        // backwards from 35% in, popping the list up at 60% opacity, zoomed
        // and tipped, and holding it there while One UI finished its slide.
        // Instead start invisible and settle out of the tapped row: a small
        // zoom around it that fades in and decelerates flat.
        val t = ((value - 1f) / 0.35f).coerceIn(0f, 1f)
        transformOrigin = TransformOrigin(pivot.x, pivot.y)
        val scale = 1f + 0.14f * t
        scaleX = scale; scaleY = scale
        rotationX = 0f; rotationY = 0f
        alpha = (1f - t).coerceIn(0f, 1f)
        return@graphicsLayer
    }
    if (tilesAnimate) {
        // W10M dive (measured from the reference video at 30fps): on launch
        // the whole Start zooms toward the viewer 1.0 -> ~1.4x, accelerating,
        // while rows fade out top first (per tile, below); black, then the
        // app. Returning, Start arrives ~1.2x and settles to 1.0 as it fades
        // in, decelerating, all within ~170ms.
        val leaving = (value - 1f).coerceIn(0f, 1f)
        val entering = 1f - MetroEnterEasing.transform(value.coerceIn(0f, 1f))
        // Dive INTO the tapped tile: zoom around its center, so the camera
        // flies at that tile and everything else flies outward and fades.
        transformOrigin = TransformOrigin(pivot.x, pivot.y)
        val scale = 1f + 0.8f * DiveZoomEasing.transform(leaving) + 0.2f * entering
        scaleX = scale
        scaleY = scale
        alpha = when {
            value >= 1.999f -> 0f
            value > 1f && !MetroSheet.exiting -> (1f - (value - 1f) / 0.35f).coerceIn(0f, 1f)
            else -> (1f - entering).coerceIn(0f, 1f)
        }
        return@graphicsLayer
    }
    val entering = 1f - MetroEnterEasing.transform(value.coerceIn(0f, 1f))
    val leaving = FastOutSlowInEasing.transform((value - 1f).coerceIn(0f, 1f))
    transformOrigin = TransformOrigin(pivot.x, pivot.y)
    val scale = 1f + leaving * 0.75f + entering * 0.12f
    scaleX = scale
    scaleY = scale
    // 3D tip toward the pivot: how far the tapped tile sits from center, in
    // each axis, -1..1. The sheet rotates around the tile point, so the side
    // you tapped swings with the dive and the far side foreshortens. The
    // return (leaving 1 -> 0) retraces it exactly; a cold entrance arrives
    // with a whisper of the opposite tip and settles flat.
    val tipX = (pivot.x - 0.5f) * 2f
    val tipY = (pivot.y - 0.5f) * 2f
    cameraDistance = 14f * density
    rotationY = tipX * (16f * leaving - 5f * entering)
    rotationX = -tipY * (12f * leaving - 4f * entering)
    // Fade steadily alongside the zoom, like W10M: halfway in, the tiles are
    // already about half gone instead of waiting until the last stretch.
    alpha = ((1f - entering) * (1f - leaving.pow(1.5f))).coerceIn(0f, 1f)
}

/** Branded tiles (Reddit, Discord, ...) stay solid: no wallpaper or glass through them. */
private fun brandTileAlpha(base: Float, brand: Boolean) = if (brand) 1f else base

private enum class StartStyle(val tileAlpha: Float, val subtitle: String) {
    Windows10Mobile(0.55f, "Glassy tiles with your background showing through"),
    WindowsPhone81(1f, "Solid-color tiles")
}

internal enum class PeopleTileStyle { WindowsPhone, Windows10Mobile }

internal object PeopleTileStyles {
    var current by mutableStateOf(PeopleTileStyle.WindowsPhone)

    fun load(context: android.content.Context) {
        val saved = context.getSharedPreferences("launcher", android.content.Context.MODE_PRIVATE)
            .getString(KEY, null)
        current = if (saved == PeopleTileStyle.WindowsPhone.name) PeopleTileStyle.WindowsPhone
        else PeopleTileStyle.Windows10Mobile
    }

    fun save(context: android.content.Context, style: PeopleTileStyle) {
        current = style
        context.getSharedPreferences("launcher", android.content.Context.MODE_PRIVATE)
            .edit().putString(KEY, style.name).apply()
    }

    private const val KEY = "people_tile_style"
}

private enum class TileKind(val terms: List<String>) {
    Phone(listOf("phone", "dialer")),
    Messages(listOf("message", "messaging", "sms", "text")),
    Browser(listOf("edge", "chrome", "browser", "internet", "firefox")),
    Photos(listOf("photos", "gallery")),
    Camera(listOf("camera")),
    Mail(listOf("mail", "gmail", "outlook", "email")),
    Calendar(listOf("calendar")),
    Maps(listOf("maps", "map")),
    Settings(listOf("settings")),
    Store(listOf("store", "market")),
    People(listOf("contacts", "people")),
    Files(listOf("files", "file manager", "my files")),
    Music(listOf("music", "spotify")),
    Clock(listOf("clock", "alarm")),
    Weather(listOf("weather")),
    Apps(emptyList())
}

private enum class AppBrand(val color: Color) {
    Discord(Color(0xFF5865F2)),
    Reddit(Color(0xFFFF4500)),
    Instagram(Color(0xFFC13584)),
    Facebook(Color(0xFF1877F2)),
    Skype(Color(0xFF00AFF0)),
    Teams(Color(0xFF4B53BC)),
    Word(Color(0xFF185ABD)),
    Excel(Color(0xFF107C41)),
    PowerPoint(Color(0xFFD24726)),
    OneNote(Color(0xFF7719AA)),
    Outlook(Color(0xFF0078D4)),
    Office(Color(0xFFD83B01)),
    YouTube(Color(0xFFFF0000))
}

private enum class TileSize(val columns: Int, val rows: Int) {
    Small(1, 1),
    Medium(2, 2),
    Wide(4, 2),
    // Kept so layouts saved by older builds still parse; Windows 10 Mobile
    // itself only offers the three sizes above.
    Large(4, 4);

    fun next() = when (this) {
        Small -> Medium
        Medium -> Wide
        Wide, Large -> Small
    }
}

private fun defaultPinnedApps(apps: List<LauncherApp>): List<Pair<LauncherApp, TileKind>> {
    val selected = mutableSetOf<LauncherApp>()
    return buildList {
        TileKind.entries.forEach { kind ->
            val candidates = apps.filter { it !in selected }
            val match = when (kind) {
                TileKind.Phone -> candidates.firstOrNull { it.isDefaultDialer }
                    ?: candidates.firstOrNull { app ->
                        app.label.contains("phone", ignoreCase = true) ||
                            app.label.contains("dialer", ignoreCase = true) ||
                            app.packageName.contains("dialer", ignoreCase = true)
                    }
                TileKind.Weather -> candidates.firstOrNull { it.pinKey() == BUILTIN_WEATHER_KEY }
                else -> candidates.firstOrNull { app ->
                    kind.terms.any { term ->
                        app.label.contains(term, ignoreCase = true) ||
                            app.packageName.contains(term, ignoreCase = true)
                    }
                }
            }
            if (match != null) {
                selected += match
                add(match to kind)
            }
        }
    }
}

private fun LauncherApp.pinKey() = "$packageName/$activityName"

internal fun canReuseLaunchableIcon(
    previousLabel: String?,
    previousUpdatedAt: Long?,
    label: String,
    updatedAt: Long
): Boolean = previousLabel == label && previousUpdatedAt == updatedAt

internal fun launchableReloadSkipped(changedPackageNames: List<String>?, hasApps: Boolean): Boolean =
    hasApps && changedPackageNames?.isEmpty() == true

/**
 * A cached on-disk icon is good until the app updates: once the package's
 * update time passes the moment the cache file was written, the icon is
 * decoded fresh (and the cache rewritten).
 */
internal fun cachedIconValid(cacheWrittenAt: Long, packageUpdatedAt: Long): Boolean =
    cacheWrittenAt > 0L && packageUpdatedAt > 0L && cacheWrittenAt >= packageUpdatedAt

private const val APP_ICON_BATCH_SIZE = 24

private fun appBrandFor(app: LauncherApp): AppBrand? {
    metroSocialBrandFor(app.packageName, app.label)?.let { brand ->
        return when (brand) {
            MetroSocialBrand.Discord -> AppBrand.Discord
            MetroSocialBrand.Reddit -> AppBrand.Reddit
            MetroSocialBrand.Facebook -> AppBrand.Facebook
            MetroSocialBrand.Instagram -> AppBrand.Instagram
            MetroSocialBrand.YouTube -> AppBrand.YouTube
            MetroSocialBrand.Teams -> AppBrand.Teams
        }
    }
    val identity = "${app.label} ${app.packageName}".lowercase(Locale.ROOT)
    return when {
        "facebook" in identity -> AppBrand.Facebook
        "teams" in identity -> AppBrand.Teams
        "skype" in identity -> AppBrand.Skype
        "word" in identity -> AppBrand.Word
        "excel" in identity -> AppBrand.Excel
        "powerpoint" in identity || "power point" in identity -> AppBrand.PowerPoint
        "onenote" in identity || "one note" in identity -> AppBrand.OneNote
        "outlook" in identity -> AppBrand.Outlook
        "microsoft 365" in identity || "office" in identity -> AppBrand.Office
        else -> null
    }
}

@Composable
private fun LauncherAppIcon(
    app: LauncherApp,
    modifier: Modifier,
    contentDescription: String? = app.label
) {
    val packIcon = IconPacks.resolved(app.packageName, app.activityName)
    val socialBrand = metroSocialBrandFor(app.packageName, app.label)
    if (packIcon != null) {
        Image(packIcon, contentDescription, modifier, contentScale = ContentScale.Fit)
    } else if (socialBrand != null) {
        MetroSocialIcon(socialBrand, contentDescription, modifier)
    } else if (app.icon.width > 1) {
        Image(
            bitmap = app.icon,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    }
}

private fun tileKindForApp(app: LauncherApp): TileKind {
    if (app.pinKey() == BUILTIN_WEATHER_KEY) return TileKind.Weather
    // Pinned shortcuts (contacts, Atlas web pages) are never live tiles: a page titled
    // "Weather news" must not turn into the Weather tile.
    if (app.packageName == PinnedShortcutStore.PACKAGE) return TileKind.Apps
    return TileKind.entries.firstOrNull { kind ->
        kind.terms.any { term ->
            app.label.contains(term, ignoreCase = true) ||
                app.packageName.contains(term, ignoreCase = true)
        }
    } ?: TileKind.Apps
}

class MainActivity : ComponentActivity() {
    private var installedApps by mutableStateOf<List<LauncherApp>>(emptyList())
    private var homeEntryCount by mutableIntStateOf(0)
    private var launcherResumed by mutableStateOf(false)
    private var pinnedAppIds by mutableStateOf<Set<String>>(emptySet())
    private var pinnedOrder by mutableStateOf<List<String>>(emptyList())
    private var pinnedShortcutApps by mutableStateOf<List<LauncherApp>>(emptyList())
    private var pinsRestored = false
    private var tileSizes by mutableStateOf<Map<String, TileSize>>(emptyMap())
    private var appsLoading by mutableStateOf(true)
    private var setupComplete by mutableStateOf(false)
    private var hadTileLayoutBeforeSetup = false
    private var setupLanguage = "en"
    private var notificationAccessEnabled by mutableStateOf(false)
    private var weatherTileState by mutableStateOf(WeatherTileState())
    private var weatherOptions by mutableStateOf(WeatherPreferences())
    private var photosTileState by mutableStateOf(
        PhotosTileState(error = "Tap to allow photo access")
    )
    private var accentColor by mutableStateOf(DefaultAccent)
    private var startStyle by mutableStateOf(StartStyle.Windows10Mobile)
    private var glassEnabled by mutableStateOf(true)
    private var wallpaperImage by mutableStateOf<ImageBitmap?>(null)
    private var wallpaperError by mutableStateOf<String?>(null)
    private var peopleTileState by mutableStateOf(
        PeopleTileState(error = "Tap to allow access to contacts")
    )
    private var wallpaperImageUri: Uri? = null
    private var appsLoadJob: Job? = null
    private var packageSequence = 0
    private val pendingAppIcon: ImageBitmap by lazy {
        android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
    }
    private var appUpdatedAt: Map<String, Long> = emptyMap()
    private var weatherLoadJob: Job? = null
    private var photosLoadJob: Job? = null
    private var peopleLoadJob: Job? = null
    private var wallpaperLoadJob: Job? = null
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var contactsPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var photosPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var wallpaperPicker: ActivityResultLauncher<PickVisualMediaRequest>

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Home gesture / button while the launcher is already in front: leave Settings
        // or All apps and show Start.
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            SettingsHome.handler?.invoke()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.content.ContextCompat.registerReceiver(this, unlockReceiver,
            android.content.IntentFilter(Intent.ACTION_USER_PRESENT), androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.any { it }) {
                refreshWeather()
            } else {
                weatherTileState = WeatherTileState(error = "Tap to allow location for weather")
            }
        }
        contactsPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                refreshPeopleContacts()
            } else {
                peopleTileState = PeopleTileState(error = "Allow contacts to see people here")
            }
        }
        photosPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.any { it }) {
                refreshPhotos()
            } else {
                photosTileState = PhotosTileState(error = "Allow photo access to show recent pictures")
            }
        }
        wallpaperPicker = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) setWallpaper(uri)
        }
        val preferences = getSharedPreferences("launcher", MODE_PRIVATE)
        StartFolderStore.load(this)
        SpartanBarAccess.autoConnect = preferences.getBoolean(SPARTAN_BAR_MODE_KEY, true)
        setupComplete = preferences.getBoolean(SETUP_COMPLETE_KEY, false)
        hadTileLayoutBeforeSetup = preferences.getBoolean(
            "setup_keep_existing_layout", preferences.contains(PINNED_APPS_KEY)
        )
        if (!preferences.contains("setup_keep_existing_layout")) {
            preferences.edit().putBoolean("setup_keep_existing_layout", hadTileLayoutBeforeSetup).apply()
        }
        setupLanguage = AppLanguage.current(this).ifEmpty {
            preferences.getString(SETUP_LANGUAGE_KEY, Locale.getDefault().language) ?: "en" }
        weatherOptions = readWeatherPreferences(preferences)
        accentColor = Color(preferences.getInt(ACCENT_COLOR_KEY, DefaultAccent.toArgb()))
        startStyle = preferences.getString(START_STYLE_KEY, null)
            ?.let { savedStyle -> StartStyle.entries.firstOrNull { it.name == savedStyle } }
            ?: StartStyle.Windows10Mobile
        glassEnabled = preferences.getBoolean(GLASS_ENABLED_KEY, true)
        PeopleTileStyles.load(this)
        IconPacks.load(this)
        preferences.getString(WALLPAPER_URI_KEY, null)?.let { value ->
            wallpaperImageUri = runCatching { Uri.parse(value) }.getOrNull()
        }
        BingWallpaperAccess.enabled = preferences.getBoolean(BING_WALLPAPER_ENABLED, false)
        if (!BingWallpaperAccess.enabled) wallpaperImageUri?.let(::loadWallpaper)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        BootScreen.showOnColdStart(this)
        setContent {
            SpartanBarConnection(launcherResumed && setupComplete)
            LaunchedEffect(SpartanBarAccess.enabled, launcherResumed, setupComplete) {
                val controller = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                controller.systemBarsBehavior =
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (SpartanBarAccess.enabled && launcherResumed && setupComplete) {
                    controller.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                } else {
                    controller.show(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                }
            }
            var handledBingRevision by remember { mutableIntStateOf(BingWallpaperAccess.revision) }
            LaunchedEffect(BingWallpaperAccess.enabled, BingWallpaperAccess.revision, launcherResumed) {
                if (BingWallpaperAccess.enabled) {
                    if (launcherResumed) {
                        var force = BingWallpaperAccess.revision != handledBingRevision
                        handledBingRevision = BingWallpaperAccess.revision
                        while (true) {
                            refreshBingWallpaper(force)
                            force = false
                            delay(60 * 60 * 1000L)
                        }
                    }
                } else {
                    wallpaperLoadJob?.cancel()
                    BingWallpaperAccess.loading = false
                    wallpaperImageUri?.let(::loadWallpaper) ?: run { wallpaperImage = null }
                }
            }
            MetroTheme(accent = accentColor, dark = true) {
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.merge(
                        TextStyle(fontFamily = MetroRegular)
                    )
                ) {
                if (!setupComplete) {
                    LauncherSetup(setupLanguage, appsLoading, ::finishSetup)
                } else LauncherScreen(
                    apps = installedApps,
                    pinnedShortcutApps = pinnedShortcutApps,
                    launcherResumed = launcherResumed,
                    homeEntryCount = homeEntryCount,
                    appsLoading = appsLoading,
                    pinnedAppIds = pinnedAppIds,
                    pinnedOrder = pinnedOrder,
                    tileSizes = tileSizes,
                    notificationAccessEnabled = notificationAccessEnabled,
                    weatherTileState = weatherTileState,
                    weatherOptions = weatherOptions,
                    onWeatherOptionsChange = ::selectWeatherOptions,
                    onRefreshWeather = ::requestWeather,
                    onUninstallApp = ::requestUninstall,
                    photosTileState = photosTileState,
                    peopleTileState = peopleTileState,
                    accentColor = accentColor,
                    startStyle = startStyle,
                    glassEnabled = glassEnabled,
                    wallpaperImage = wallpaperImage,
                    wallpaperError = wallpaperError,
                    onOpenWallpaperPicker = {
                        wallpaperPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onClearWallpaper = ::clearLauncherWallpaper,
                    onSelectAccent = ::selectAccent,
                    onSelectStartStyle = ::selectStartStyle,
                    onGlassEnabledChange = ::selectGlassEnabled,
                    onRequestContacts = ::requestContacts,
                    onRequestPhotos = ::requestPhotos,
                    onOpenNotificationSettings = {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    onTogglePinnedApp = ::togglePinnedApp,
                    onMovePinnedApp = ::movePinnedApp,
                    onResizePinnedApp = ::resizePinnedApp,
                    onLaunch = ::launchApp
                )
                }
            }
        }
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(unlockReceiver) }
        super.onDestroy()
    }

    private val unlockReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) playUnlockEntrance()
        }
    }

    /** SpartanLock hides on USER_PRESENT too, so Start animates in as the lock goes. */
    private var unlockPending = false
    private var unlockTimeout: Job? = null
    private fun playUnlockEntrance() {
        if (!setupComplete) return
        unlockPending = true
        lifecycleScope.launch { MetroSheet.unlock.snapTo(0f) }
        if (window.decorView.hasWindowFocus() && launcherResumed) startUnlockAnimation()
        unlockTimeout?.cancel()
        // Unlocked straight into another app: don't leave Start hidden.
        unlockTimeout = lifecycleScope.launch {
            delay(4000)
            if (unlockPending) { unlockPending = false; MetroSheet.unlock.snapTo(1f) }
        }
    }

    private fun startUnlockAnimation() {
        if (!unlockPending) return
        unlockPending = false
        unlockTimeout?.cancel()
        lifecycleScope.launch {
            // One UI presents the window a frame or two after focus.
            delay(80)
            MetroSheet.unlock.animateTo(1f, tween(1150, easing = LinearEasing))
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) startUnlockAnimation()
    }

    override fun onResume() {
        CarrierName.register(this)
        super.onResume()
        launcherResumed = true
        refreshPinnedShortcuts()
        CalendarAccess.revision++
        homeEntryCount++
        if (weatherOptions.place != null || hasLocationPermission()) refreshWeather()
        if (hasContactsPermission()) {
            refreshPeopleContacts()
        } else {
            peopleTileState = PeopleTileState(error = "Tap to allow access to contacts")
        }
        if (hasPhotosPermission()) {
            refreshPhotos()
        } else {
            photosTileState = PhotosTileState(error = "Tap to allow photo access")
        }
        notificationAccessEnabled =
            packageName in NotificationManagerCompat.getEnabledListenerPackages(this)
        LauncherNotificationListener.refresh(this)
        appsLoadJob?.cancel()
        appsLoadJob = lifecycleScope.launch {
            val sequence = packageSequence
            val changed = withContext(Dispatchers.IO) { readChangedPackages(sequence) }
            val changedNames = changed?.second
            if (changed != null && changed.second.isEmpty()) packageSequence = changed.first
            if (launchableReloadSkipped(changedNames, installedApps.isNotEmpty())) {
                appsLoading = false
                return@launch
            }
            if (installedApps.isEmpty()) appsLoading = true
            val previousApps = installedApps
            val previousUpdatedAt = appUpdatedAt
            val preview = withContext(Dispatchers.IO) {
                loadLaunchableApps(previousApps, previousUpdatedAt, decodeIcons = false)
            }
            restorePins(preview.apps)
            installedApps = preview.apps
            appUpdatedAt = preview.updatedAt
            appsLoading = false
            val loaded = withContext(Dispatchers.IO) {
                loadLaunchableApps(preview.apps, preview.updatedAt, decodeIcons = true)
            }
            if (coroutineContext[Job]?.isActive == false) return@launch
            installedApps = loaded.apps
            appUpdatedAt = loaded.updatedAt
            val latestSequence = withContext(Dispatchers.IO) { readCurrentPackageSequence() }
            if (coroutineContext[Job]?.isActive != false && latestSequence != null) {
                packageSequence = latestSequence
            }
        }
    }

    override fun onPause() {
        launcherResumed = false
        currentFocus?.let { v ->
            getSystemService(android.view.inputmethod.InputMethodManager::class.java)
                ?.hideSoftInputFromWindow(v.windowToken, 0)
            v.clearFocus()
        }
        super.onPause()
    }

    private fun finishSetup(language: String) {
        if (appsLoading || setupComplete) return
        if (!hadTileLayoutBeforeSetup) {
            val defaults = defaultPinnedApps(installedApps)
            pinnedOrder = defaults.map { (app, _) -> app.pinKey() }
            pinnedAppIds = pinnedOrder.toSet()
            tileSizes = defaults.associate { (app, kind) ->
                app.pinKey() to when (kind) {
                    TileKind.Calendar, TileKind.Photos -> TileSize.Wide
                    TileKind.Phone, TileKind.Messages, TileKind.Browser, TileKind.People,
                    TileKind.Mail, TileKind.Weather, TileKind.Music -> TileSize.Medium
                    else -> TileSize.Small
                }
            }
            saveTileConfiguration(pinnedAppIds, pinnedOrder, tileSizes)
        }
        setupLanguage = language
        AppLanguage.save(this, setupAppLanguage(language))
        getSharedPreferences("launcher", MODE_PRIVATE).edit()
            .putString(SETUP_LANGUAGE_KEY, language)
            .putBoolean(SETUP_COMPLETE_KEY, true).apply()
        setupComplete = true
        homeEntryCount++
    }

    private fun refreshPinnedShortcuts() {
        lifecycleScope.launch {
            val loaded = withContext(Dispatchers.IO) { PinnedShortcutStore.loadApps(this@MainActivity) }
            pinnedShortcutApps = loaded
            // Memory order is empty until restorePins. Writing it here replaces every other tile.
            if (!pinsRestored) return@launch
            adoptShortcutPins(loaded.map { it.pinKey() })
        }
    }

    private fun adoptShortcutPins(keys: List<String>) {
        val merged = mergeShortcutPins(ShortcutPinMerge(pinnedAppIds, pinnedOrder), keys)
        if (merged.pins == pinnedAppIds && merged.order == pinnedOrder) return
        pinnedAppIds = merged.pins
        pinnedOrder = merged.order
        saveTileConfiguration(pinnedAppIds, pinnedOrder, tileSizes)
    }

    private fun togglePinnedApp(app: LauncherApp) {
        val updated = pinnedAppIds.toMutableSet()
        if (!updated.add(app.pinKey())) {
            updated.remove(app.pinKey())
            if (app.packageName == PinnedShortcutStore.PACKAGE) {
                PinnedShortcutStore.remove(this, app.pinKey())
                pinnedShortcutApps = pinnedShortcutApps.filterNot { it.pinKey() == app.pinKey() }
            }
        }
        pinnedAppIds = updated
        val key = app.pinKey()
        pinnedOrder = if (key in updated) (pinnedOrder + key).distinct() else pinnedOrder - key
        saveTileConfiguration(updated, pinnedOrder, tileSizes)
    }

    private fun movePinnedApp(app: LauncherApp, delta: Int) {
        val from = pinnedOrder.indexOf(app.pinKey())
        if (from < 0) return
        val to = (from + delta).coerceIn(0, pinnedOrder.lastIndex)
        if (from == to) return
        val updated = pinnedOrder.toMutableList().apply { add(to, removeAt(from)) }
        pinnedOrder = updated
        saveTileConfiguration(pinnedAppIds, updated, tileSizes)
    }

    private fun resizePinnedApp(app: LauncherApp, size: TileSize) {
        tileSizes = tileSizes + (app.pinKey() to size)
        saveTileConfiguration(pinnedAppIds, pinnedOrder, tileSizes)
    }

    private fun saveTileConfiguration(
        pins: Set<String>,
        order: List<String>,
        sizes: Map<String, TileSize>
    ) {
        getSharedPreferences("launcher", MODE_PRIVATE).edit()
            .putStringSet(PINNED_APPS_KEY, pins.toHashSet())
            .putString(PINNED_ORDER_KEY, order.joinToString("\n"))
            .putStringSet(TILE_SIZES_KEY, sizes.map { (key, size) -> "$key|${size.name}" }.toHashSet())
            .apply()
    }

    private fun restorePins(apps: List<LauncherApp>) {
        val preferences = getSharedPreferences("launcher", MODE_PRIVATE)
        val storedPins = preferences.getStringSet(PINNED_APPS_KEY, null)
        pinnedAppIds = when {
            storedPins != null -> storedPins.toSet()
            apps.isNotEmpty() -> defaultPinnedApps(apps)
                .mapTo(mutableSetOf()) { (app, _) -> app.pinKey() }
                .also { defaults ->
                    preferences.edit().putStringSet(PINNED_APPS_KEY, defaults).apply()
                }
            else -> emptySet()
        }
        val weatherPinMigrated = preferences.getBoolean(WEATHER_PIN_MIGRATED_V2_KEY, false)
        if (storedPins != null && !weatherPinMigrated && apps.any { it.pinKey() == BUILTIN_WEATHER_KEY }) {
            pinnedAppIds = pinnedAppIds + BUILTIN_WEATHER_KEY
            preferences.edit().putStringSet(PINNED_APPS_KEY, pinnedAppIds).apply()
        }
        preferences.edit().putBoolean(WEATHER_PIN_MIGRATED_V2_KEY, true).apply()
        val validPins = apps.filter { it.pinKey() in pinnedAppIds }.map { it.pinKey() } +
            PinnedShortcutStore.keys(this)
        val storedOrder = preferences.getString(PINNED_ORDER_KEY, null)
        val savedOrder = storedOrder
            ?.split('\n')
            .orEmpty()
            .filter { it in validPins }
        val initialOrder = if (storedOrder == null) {
            defaultPinnedApps(apps).map { (app, _) -> app.pinKey() }
        } else {
            savedOrder
        }
        val nextOrder = (initialOrder + validPins.filterNot { it in initialOrder }).distinct()
        tileSizes = preferences.getStringSet(TILE_SIZES_KEY, emptySet())
            .orEmpty()
            .mapNotNull { entry ->
                val separator = entry.lastIndexOf('|')
                if (separator < 0) return@mapNotNull null
                val key = entry.substring(0, separator)
                val size = TileSize.entries.firstOrNull { it.name == entry.substring(separator + 1) }
                if (key in validPins && size != null) key to size else null
            }
            .toMap()
        val merged = mergeShortcutPins(ShortcutPinMerge(pinnedAppIds, nextOrder), PinnedShortcutStore.keys(this))
        pinnedAppIds = merged.pins
        pinnedOrder = merged.order
        if (storedPins?.toSet() != merged.pins || storedOrder != merged.order.joinToString("\n")) {
            saveTileConfiguration(merged.pins, merged.order, tileSizes)
        }
        pinsRestored = true
        adoptShortcutPins(pinnedShortcutApps.map { it.pinKey() })
    }

    private fun readChangedPackages(sequence: Int): Pair<Int, List<String>>? {
        val changed = runCatching { packageManager.getChangedPackages(sequence) }.getOrNull()
            ?: return null
        return changed.sequenceNumber to changed.packageNames
    }

    private fun readCurrentPackageSequence(): Int? =
        runCatching { packageManager.getChangedPackages(Int.MAX_VALUE) }.getOrNull()?.sequenceNumber

    private fun appIconCacheFile(key: String): java.io.File {
        val dir = java.io.File(cacheDir, "app-icons").apply { mkdirs() }
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return java.io.File(dir, "$digest.png")
    }

    /** The icon decoded for [key] on an earlier start, if the app hasn't updated since. */
    private fun loadCachedAppIcon(key: String, updated: Long): ImageBitmap? {
        val file = appIconCacheFile(key)
        if (!cachedIconValid(file.lastModified(), updated)) return null
        return runCatching {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        }.getOrNull()
    }

    /** Asks PackageManager for the icon, and caches the small decoded copy for next time. */
    private fun loadFreshAppIcon(
        result: android.content.pm.ResolveInfo,
        key: String,
        updated: Long
    ): ImageBitmap? {
        val bitmap = runCatching {
            result.loadIcon(packageManager).toBitmap(width = 96, height = 96)
        }.getOrNull() ?: return null
        runCatching {
            java.io.FileOutputStream(appIconCacheFile(key)).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
        }
        return bitmap.asImageBitmap()
    }

    /** Re-resolves every app icon after the icon pack changes. */
    internal fun reloadIconsForPack() {
        lifecycleScope.launch {
            val loaded = withContext(Dispatchers.IO) { loadLaunchableApps(installedApps, emptyMap(), decodeIcons = true) }
            installedApps = loaded.apps
            appUpdatedAt = loaded.updatedAt
        }
    }

    private suspend fun loadLaunchableApps(
        previousApps: List<LauncherApp>,
        previousUpdatedAt: Map<String, Long>,
        decodeIcons: Boolean
    ): LoadedApps {
        val previousByKey = previousApps.associateBy { it.pinKey() }
        val packageUpdatedAt = if (decodeIcons) {
            packageManager.getInstalledPackages(0)
                .associate { it.packageName to it.lastUpdateTime }
        } else {
            emptyMap()
        }
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val defaultDialerPackage = getSystemService(TelecomManager::class.java)?.defaultDialerPackage
        val results = packageManager.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { result -> result.activityInfo?.let { result to it } }
        // Labels and icons are hundreds of small PackageManager calls; one
        // after another on a single thread they were most of the cold-start
        // wait, so work through them in parallel batches.
        val loaded = coroutineScope {
            results.chunked(APP_ICON_BATCH_SIZE).flatMap { batch ->
                batch.map { (result, activity) ->
                    async {
                        val label = activity.loadLabel(packageManager).toString()
                        val key = "${activity.packageName}/${activity.name}"
                        val updated = if (decodeIcons) packageUpdatedAt[activity.packageName] ?: 0L
                            else previousUpdatedAt[key] ?: 0L
                        val previous = previousByKey[key]
                        val icon = when {
                            !decodeIcons -> previous?.icon ?: pendingAppIcon
                            IconPacks.current.isNotEmpty() -> IconPacks.iconFor(this@MainActivity, activity.packageName, activity.name)
                                ?: loadCachedAppIcon(key, updated) ?: loadFreshAppIcon(result, key, updated)
                                ?: previous?.icon ?: pendingAppIcon
                            previous?.icon != null && previous.icon.width > 1 &&
                                canReuseLaunchableIcon(previous.label, previousUpdatedAt[key], label, updated) ->
                                previous.icon
                            else -> loadCachedAppIcon(key, updated)
                                ?: loadFreshAppIcon(result, key, updated)
                                ?: previous?.icon ?: pendingAppIcon
                        }
                        Triple(
                            key, updated,
                            LauncherApp(
                                label = label,
                                packageName = activity.packageName,
                                activityName = activity.name,
                                icon = icon,
                                isDefaultDialer = activity.packageName == defaultDialerPackage,
                                canUninstall = activity.packageName != packageName &&
                                    (activity.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM == 0 ||
                                        activity.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0)
                            )
                        )
                    }
                }.awaitAll()
            }
        }
        val updatedAt = loaded.associate { it.first to it.second }.toMutableMap()
        val launchableApps = loaded.map { it.third }
            .distinctBy { it.packageName to it.activityName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
        val selfUpdatedAt = if (decodeIcons) packageUpdatedAt[packageName] ?: 0L
            else previousUpdatedAt[BUILTIN_WEATHER_KEY] ?: 0L
        updatedAt[BUILTIN_WEATHER_KEY] = selfUpdatedAt
        val previousWeather = previousByKey[BUILTIN_WEATHER_KEY]
        val weatherTile = previousWeather?.takeIf {
            !decodeIcons || (it.icon.width > 1 && canReuseLaunchableIcon(
                it.label, previousUpdatedAt[BUILTIN_WEATHER_KEY], "Weather", selfUpdatedAt
            ))
        } ?: LauncherApp(
            label = "Weather",
            packageName = BUILTIN_WEATHER_PACKAGE,
            activityName = BUILTIN_WEATHER_ACTIVITY,
            icon = if (!decodeIcons) pendingAppIcon else runCatching {
                packageManager.getApplicationIcon(packageName)
                    .toBitmap(width = 96, height = 96)
                    .asImageBitmap()
            }.getOrElse { pendingAppIcon },
            isDefaultDialer = false
        )
        return LoadedApps(launchableApps + weatherTile, updatedAt)
    }

    private fun launchApp(app: LauncherApp, sourceRect: android.graphics.Rect? = null) {
        if (app.pinKey() == BUILTIN_WEATHER_KEY) {
            requestWeather()
            return
        }
        // Ask for the app to grow out of the tapped tile, the way Windows 10
        // Mobile dives into it. Some systems ignore this (Android 15+
        // dropped custom launch animations for apps targeting recent SDKs,
        // and Samsung plays its own transition); there, the tile dive on our
        // side carries the effect and this is simply unused. Launches
        // without a source (folder children) fall back to the fade pair.
        // W10M: the app materializes full-screen at the end of the dive
        // (not Android's grow-from-tile), so always use the dive pair.
        // One UI ignores makeScaleUpAnimation (its card slide played instead,
        // verified by screenrecord); the pending override is honored, so the
        // app materializes at the end of our dive into the tile.
        val options = if (sourceRect != null && !sourceRect.isEmpty) {
            ActivityOptions.makeClipRevealAnimation(
                window.decorView, sourceRect.left, sourceRect.top,
                sourceRect.width(), sourceRect.height()
            ).toBundle()
        } else w10mLaunchOptions()
        if (app.packageName == PinnedShortcutStore.PACKAGE) {
            PinnedShortcutStore.launch(this, app.pinKey(), options)
            if (sourceRect == null) applyW10mOpenTransition()
            return
        }
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(ComponentName(app.packageName, app.activityName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
            options
        )
        if (sourceRect == null) applyW10mOpenTransition()
    }

    /**
     * Same transition as [w10mLaunchOptions], registered the modern way.
     * On Android 14+ the bundle route is deprecated and Samsung's shell
     * transitions ignore it (its sideways card slide played instead); this
     * override is the one the system still honors there.
     */
    @Suppress("DEPRECATION")
    private fun applyW10mOpenTransition() {
        // overrideActivityTransition only styles transitions *into this*
        // activity; for the app we just started, the pending override is
        // the one that applies (otherwise Samsung's card slide plays).
        overridePendingTransition(R.anim.w10m_app_enter, R.anim.w10m_app_exit)
    }

    /**
     * Window transition for opening an app, Windows 10 Mobile style: the app
     * fades in full-size over our tile zoom. Without this, Samsung plays its
     * own sideways card slide after our zoom and the two animations clash.
     */
    private fun w10mLaunchOptions(): Bundle? =
        ActivityOptionsCompat.makeCustomAnimation(
            this, R.anim.w10m_app_enter, R.anim.w10m_app_exit
        ).toBundle()

    private fun requestWeather() {
        if (weatherOptions.place != null || hasLocationPermission()) {
            refreshWeather(force = true)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun hasLocationPermission() =
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasContactsPermission() =
        checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    private fun hasPhotosPermission(): Boolean = when {
        Build.VERSION.SDK_INT >= 34 ->
            checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
        Build.VERSION.SDK_INT >= 33 ->
            checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        else ->
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPhotos() {
        if (hasPhotosPermission()) {
            refreshPhotos()
        } else {
            val permissions = when {
                Build.VERSION.SDK_INT >= 34 -> arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
                Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            photosPermissionLauncher.launch(permissions)
        }
    }

    private fun refreshPhotos() {
        photosLoadJob?.cancel()
        photosLoadJob = lifecycleScope.launch {
            val ids = recentPhotoIds(this@MainActivity)
            if (!shouldReloadPhotos(photosTileState.photoIds, ids, photosTileState.error != null)) return@launch
            photosTileState = photosTileState.copy(loading = true, error = null)
            try {
                photosTileState = if (ids == null) {
                    PhotosTileState(error = "Could not read your photo library")
                } else {
                    loadRecentPhotos(this@MainActivity, ids)
                }
            } catch (exception: SecurityException) {
                photosTileState = PhotosTileState(error = "Allow photo access to show recent pictures")
            } catch (exception: java.io.IOException) {
                photosTileState = photosTileState.copy(loading = false, error = "Photos are temporarily unavailable")
            }
        }
    }

    private fun requestContacts() {
        if (hasContactsPermission()) {
            refreshPeopleContacts()
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun refreshPeopleContacts() {
        peopleLoadJob?.cancel()
        peopleLoadJob = lifecycleScope.launch {
            peopleTileState = peopleTileState.copy(loading = true, error = null)
            try {
                peopleTileState = loadPeopleContacts(this@MainActivity)
            } catch (exception: SecurityException) {
                peopleTileState = PeopleTileState(error = "Allow contacts to see people here")
            } catch (exception: java.io.IOException) {
                peopleTileState = peopleTileState.copy(loading = false, error = "Contacts are temporarily unavailable")
            }
        }
    }

    private fun selectWeatherOptions(options: WeatherPreferences) {
        val locationOrUnitsChanged = options.place != weatherOptions.place || options.units != weatherOptions.units
        weatherOptions = options
        saveWeatherPreferences(getSharedPreferences("launcher", MODE_PRIVATE), options)
        weatherTileState = if (locationOrUnitsChanged) {
            WeatherTileState(locationName = options.place?.name ?: "Current location",
                units = options.units, showBackground = options.showBackground)
        } else weatherTileState.copy(showBackground = options.showBackground)
        if (locationOrUnitsChanged) requestWeather()
    }

    private fun requestUninstall(app: LauncherApp) {
        if (!app.canUninstall || app.pinKey() == BUILTIN_WEATHER_KEY || app.packageName == packageName) return
        try {
            startActivity(Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.packageName, null)))
        } catch (_: android.content.ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", app.packageName, null)))
        }
    }

    private fun refreshWeather(force: Boolean = false) {
        val options = weatherOptions
        if (!weatherNeedsRefresh(
                updatedAt = weatherTileState.updatedAt,
                now = System.currentTimeMillis(),
                unitsMatch = weatherTileState.units == options.units,
                hasReading = weatherTileState.temperature != null,
                force = force
            )
        ) return
        weatherLoadJob?.cancel()
        weatherLoadJob = lifecycleScope.launch {
            weatherTileState = weatherTileState.copy(loading = true, error = null)
            try {
                val updated = loadCurrentWeather(this@MainActivity, options)
                weatherTileState = updated.copy(showBackground = weatherOptions.showBackground)
            } catch (_: SecurityException) {
                weatherTileState = weatherTileState.copy(loading = false,
                    error = "Allow location access or choose a city in Settings → Weather")
            } catch (exception: java.io.IOException) {
                weatherTileState = weatherTileState.copy(loading = false, error = exception.message ?: "Weather unavailable")
            } catch (_: org.json.JSONException) {
                weatherTileState = weatherTileState.copy(loading = false, error = "Weather data unavailable")
            }
        }
    }

    private fun setWallpaper(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (exception: SecurityException) {
            wallpaperError = "Could not save access to that photo"
            return
        }
        wallpaperImageUri = uri
        BingWallpaperAccess.enabled = false
        getSharedPreferences("launcher", MODE_PRIVATE).edit()
            .putBoolean(BING_WALLPAPER_ENABLED, false)
            .putString(WALLPAPER_URI_KEY, uri.toString())
            .apply()
        loadWallpaper(uri)
    }

    private fun refreshBingWallpaper(force: Boolean) {
        if (!BingWallpaperAccess.enabled || wallpaperLoadJob?.isActive == true) return
        wallpaperLoadJob = lifecycleScope.launch {
            BingWallpaperAccess.loading = true
            BingWallpaperAccess.error = null
            try {
                if (wallpaperImage == null) {
                    wallpaperImage = cachedBingWallpaper(this@MainActivity)
                    BingWallpaperAccess.image = wallpaperImage
                    BingWallpaperAccess.credit = getSharedPreferences("launcher", MODE_PRIVATE)
                        .getString("bing_wallpaper_credit", "").orEmpty()
                }
                val (image, credit) = loadBingWallpaper(this@MainActivity, force)
                if (BingWallpaperAccess.enabled) {
                    wallpaperImage = image
                    wallpaperError = null
                    BingWallpaperAccess.image = image
                    BingWallpaperAccess.credit = credit
                }
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (_: Exception) {
                BingWallpaperAccess.error = "Could not refresh Bing wallpaper. Your current background is kept. Try again when online."
            } finally { BingWallpaperAccess.loading = false }
        }
    }

    private fun loadWallpaper(uri: Uri) {
        wallpaperLoadJob?.cancel()
        wallpaperLoadJob = lifecycleScope.launch {
            wallpaperError = null
            try {
                val image = withContext(Dispatchers.IO) {
                    decodeWallpaper(uri)
                }
                wallpaperImage = image
                if (image == null) wallpaperError = "Could not load that photo"
            } catch (exception: java.io.IOException) {
                wallpaperImage = null
                wallpaperError = "Could not read that photo"
            } catch (exception: SecurityException) {
                wallpaperImage = null
                wallpaperError = "Photo access has expired. Choose it again."
            }
        }
    }

    private fun decodeWallpaper(uri: Uri): ImageBitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val width = info.size.width
                val height = info.size.height
                val scale = minOf(
                    1f,
                    MAX_WALLPAPER_DIMENSION.toFloat() / maxOf(width, height)
                )
                decoder.setTargetSize(
                    (width * scale).toInt().coerceAtLeast(1),
                    (height * scale).toInt().coerceAtLeast(1)
                )
            }.asImageBitmap()
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_WALLPAPER_DIMENSION ||
            bounds.outHeight / sampleSize > MAX_WALLPAPER_DIMENSION
        ) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
        }
    }

    private fun clearLauncherWallpaper() {
        wallpaperLoadJob?.cancel()
        BingWallpaperAccess.enabled = false
        wallpaperImageUri = null
        wallpaperImage = null
        wallpaperError = null
        getSharedPreferences("launcher", MODE_PRIVATE).edit()
            .putBoolean(BING_WALLPAPER_ENABLED, false)
            .remove(WALLPAPER_URI_KEY)
            .apply()
    }

    private fun selectAccent(color: Color) {
        accentColor = color
        getSharedPreferences("launcher", MODE_PRIVATE).edit()
            .putInt(ACCENT_COLOR_KEY, color.toArgb())
            .apply()
        contentResolver.notifyChange(AccentColorProvider.COLOR_URI, null)
    }

    private fun selectStartStyle(style: StartStyle) {
        startStyle = style
        getSharedPreferences("launcher", MODE_PRIVATE).edit()
            .putString(START_STYLE_KEY, style.name)
            .apply()
    }

    private fun selectGlassEnabled(enabled: Boolean) {
        glassEnabled = enabled
        getSharedPreferences("launcher", MODE_PRIVATE).edit()
            .putBoolean(GLASS_ENABLED_KEY, enabled)
            .apply()
    }

    private companion object {
        const val PINNED_APPS_KEY = "pinned_apps"
        const val PINNED_ORDER_KEY = "pinned_order"
        const val TILE_SIZES_KEY = "tile_sizes"
        const val WEATHER_PIN_MIGRATED_V2_KEY = "weather_pin_migrated_v2"
        const val ACCENT_COLOR_KEY = "launcher_accent_color"
        const val START_STYLE_KEY = "launcher_start_style"
        const val GLASS_ENABLED_KEY = "launcher_glass_enabled"
        const val WALLPAPER_URI_KEY = "launcher_wallpaper_uri"
        const val MAX_WALLPAPER_DIMENSION = 3840
    }
}

private const val BUILTIN_WEATHER_PACKAGE = "com.ken1dot.spartanlauncher.builtin"
private const val BUILTIN_WEATHER_ACTIVITY = "WeatherTile"
private const val BUILTIN_WEATHER_KEY = "$BUILTIN_WEATHER_PACKAGE/$BUILTIN_WEATHER_ACTIVITY"

@Composable
private fun LauncherScreen(
    launcherResumed: Boolean,
    apps: List<LauncherApp>,
    pinnedShortcutApps: List<LauncherApp> = emptyList(),
    homeEntryCount: Int,
    appsLoading: Boolean,
    pinnedAppIds: Set<String>,
    pinnedOrder: List<String>,
    tileSizes: Map<String, TileSize>,
    notificationAccessEnabled: Boolean,
    weatherTileState: WeatherTileState,
    weatherOptions: WeatherPreferences,
    onWeatherOptionsChange: (WeatherPreferences) -> Unit,
    onRefreshWeather: () -> Unit,
    onUninstallApp: (LauncherApp) -> Unit,
    photosTileState: PhotosTileState,
    peopleTileState: PeopleTileState,
    accentColor: Color,
    startStyle: StartStyle,
    glassEnabled: Boolean,
    wallpaperImage: ImageBitmap?,
    wallpaperError: String?,
    onOpenWallpaperPicker: () -> Unit,
    onClearWallpaper: () -> Unit,
    onSelectAccent: (Color) -> Unit,
    onSelectStartStyle: (StartStyle) -> Unit,
    onGlassEnabledChange: (Boolean) -> Unit,
    onRequestContacts: () -> Unit,
    onRequestPhotos: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onTogglePinnedApp: (LauncherApp) -> Unit,
    onMovePinnedApp: (LauncherApp, Int) -> Unit,
    onResizePinnedApp: (LauncherApp, TileSize) -> Unit,
    onLaunch: LaunchHandler
) {
    val notificationCounts by LauncherNotificationCounts.counts.collectAsState()
    val notificationPreviews by LauncherNotificationCounts.previews.collectAsState()
    val musicSessions by LauncherMediaPlayback.sessions.collectAsState()
    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val hazeState = rememberHazeState()
    val glassTiles = useGlassTiles(
        windows10Mobile = startStyle == StartStyle.Windows10Mobile,
        hasWallpaper = wallpaperImage != null,
        enabled = glassEnabled
    )
    // Windows 10 Mobile only: one shade over the whole wallpaper, deepening
    // uniformly as the app list slides in — Start's shade on Start, the app
    // list's deeper shade once settled. Because it is screen-wide there is no
    // seam between the two pages mid-swipe. Windows Phone 8.1 has no shade.
    val startWallpaperShade = if (glassTiles) 0.22f else 0.26f
    val appListWallpaperShade = 0.7816f // Settled app list shade: 0.48 over 0.58 black.
    val wallpaperShade = if (startStyle == StartStyle.Windows10Mobile) {
        val pagePosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
        val progress = (1f - kotlin.math.abs(1f - pagePosition)).coerceIn(0f, 1f)
        // Ease-out so most of the deepening happens early in the swipe.
        val eased = 1f - (1f - progress) * (1f - progress)
        startWallpaperShade + (appListWallpaperShade - startWallpaperShade) * eased
    } else appListWallpaperShade
    // The sticky letter headings read the shade from state in their draw phase;
    // a plain Float gets captured once by their composed modifier and goes stale.
    val wallpaperShadeState = remember { mutableFloatStateOf(wallpaperShade) }
    SideEffect { wallpaperShadeState.floatValue = wallpaperShade }
    val scope = rememberCoroutineScope()
    var startExiting by remember { mutableStateOf(false) }
    var startEditing by remember { mutableStateOf(false) }
    var appsJumping by remember { mutableStateOf(false) }
    var showLauncherSettings by rememberSaveable { mutableStateOf(false) }
    var settingsExiting by remember { mutableStateOf(false) }
    var settingsReturnCount by rememberSaveable { mutableIntStateOf(0) }
    val openLauncherSettings: () -> Unit = {
        if (!startExiting && !showLauncherSettings) {
            startExiting = true
            scope.launch {
                delay(METRO_EXIT_DURATION.toLong())
                settingsExiting = false
                showLauncherSettings = true
                startExiting = false
            }
        }
    }
    val closeLauncherSettings: () -> Unit = {
        if (!settingsExiting) {
            settingsExiting = true
            scope.launch {
                delay(METRO_EXIT_DURATION.toLong())
                settingsReturnCount++
                showLauncherSettings = false
                settingsExiting = false
            }
        }
    }
    LaunchedEffect(homeEntryCount) {
        startExiting = false
    }
    val calendarContext = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(showLauncherSettings, SpartanBarAccess.enabled, launcherResumed) {
        if (launcherResumed) sendSpartanBarAppearance(calendarContext,
            if (SpartanBarAccess.enabled && !showLauncherSettings)
                android.graphics.Color.TRANSPARENT else SettingsBackground.toArgb())
    }
    val calendarPermission = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { CalendarAccess.revision++ }
    val launchFromStart: LaunchHandler = { app, sourceRect ->
        if (tileKindForApp(app) == TileKind.Calendar &&
            calendarContext.checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED &&
            !calendarContext.getSharedPreferences("launcher", android.content.Context.MODE_PRIVATE)
                .getBoolean("calendar_permission_requested", false)) {
            calendarContext.getSharedPreferences("launcher", android.content.Context.MODE_PRIVATE)
                .edit().putBoolean("calendar_permission_requested", true).apply()
            calendarPermission.launch(Manifest.permission.READ_CALENDAR)
        } else if (app.packageName == "com.android.settings") {
            openLauncherSettings()
        } else if (app.pinKey() == BUILTIN_WEATHER_KEY) {
            onLaunch(app, null)
        } else if (!startExiting) {
            startExiting = true
            scope.launch {
                // Hand off as the dive completes. Windows 10 Mobile fades to
                // black, holds a beat, then the app arrives; starting the app
                // mid-fade instead let Samsung's own window slide cut across
                // our zoom while the tiles were still visible.
                delay(METRO_EXIT_DURATION.toLong())
                onLaunch(app, sourceRect)
            }
        }
    }
    BackHandler(enabled = pagerState.currentPage == 1) {
        scope.launch { pagerState.animateScrollToPage(0) }
        searchQuery = ""
    }
    SettingsHome.handler = {
        scope.launch { pagerState.animateScrollToPage(0) }
        searchQuery = ""
        if (showLauncherSettings) closeLauncherSettings()
    }
    BackHandler(enabled = showLauncherSettings) {
        // A settings sub-page goes back to the settings list first (W10M back stack).
        if (SettingsBack.handler?.invoke() != true) closeLauncherSettings()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(if (showLauncherSettings) SettingsBackground else MetroTheme.colors.bg)
            .onGloballyPositioned { rootSize = it.size }
    ) {
        if (
            wallpaperImage != null &&
            !showLauncherSettings &&
            startStyle == StartStyle.Windows10Mobile
        ) {
            Image(
                bitmap = wallpaperImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().then(
                    if (glassTiles) Modifier.hazeSource(hazeState) else Modifier
                )
            )
            // Screen-wide wallpaper shade; see wallpaperShade above.
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = wallpaperShade)))
        }
        if (showLauncherSettings) {
            val settingsMotion = rememberMetroMotion(settingsExiting)
            LauncherSettingsScreen(
                accentColor = accentColor,
                notificationAccessEnabled = notificationAccessEnabled,
                weather = weatherTileState,
                weatherOptions = weatherOptions,
                onWeatherOptionsChange = onWeatherOptionsChange,
                onRefreshWeather = onRefreshWeather,
                hasWallpaper = wallpaperImage != null,
                wallpaperError = wallpaperError,
                onOpenWallpaperPicker = onOpenWallpaperPicker,
                onOpenNotificationSettings = onOpenNotificationSettings,
                onClearWallpaper = onClearWallpaper,
                onSelectAccent = onSelectAccent,
                startStyle = startStyle,
                glassEnabled = glassEnabled,
                onGlassEnabledChange = onGlassEnabledChange,
                onSelectStartStyle = onSelectStartStyle,
                modifier = Modifier
                    .fillMaxSize()
                    .launcherSystemPadding()
                    .metroSwivel { settingsMotion.value }
            )
        } else {
            HorizontalPager(
                state = pagerState,
                // Keep both pages composed: otherwise swiping disposes the
                // off-screen page and it replays its whole entrance (blank
                // beat, zoom, fade) every time it slides in — the All apps
                // "jump". W10M's list just slides in with the swipe.
                beyondViewportPageCount = 1,
                userScrollEnabled = !startEditing && !startExiting && !appsJumping,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                if (page == 0) {
                    // Each page keeps its dive to itself: clip so the zoom
                    // can't spill onto the neighbour, and stay hidden while
                    // the other page is the one animating.
                    Box(Modifier.fillMaxSize().clipToBounds().graphicsLayer {
                        alpha = if (pagerState.currentPage != 0 && startExiting) 0f else 1f
                    }) {
                    // W10M dive goes to black: the wallpaper disappears
                    // behind the zooming tiles, and returning it comes back
                    // out of black.
                    // Drawn on top (zIndex) so tiles, the All apps link and
                    // the wallpaper are all gone before the app takes over.
                    Box(Modifier.fillMaxSize().zIndex(10f).graphicsLayer {
                        val v = MetroSheet.progress()
                        alpha = when {
                            v <= 1f -> 0f
                            MetroSheet.exiting -> ((v - 1.4f) / 0.3f).coerceIn(0f, 1f)
                            else -> ((v - 1f) / 0.35f).coerceIn(0f, 1f)
                        }
                    }.background(Color.Black))
                    StartTiles(
                        onEditingChange = { startEditing = it },
                        liveTilesActive = launcherResumed && pagerState.currentPage == 0 && !startExiting && !MetroSheet.pressing,
                        homeEntryCount = homeEntryCount + settingsReturnCount,
                        isExiting = startExiting && pagerState.currentPage == 0,
                        apps = apps + pinnedShortcutApps,
                        appsLoading = appsLoading,
                        pinnedAppIds = pinnedAppIds,
                        pinnedOrder = pinnedOrder,
                        tileSizes = tileSizes,
                        notificationCounts = notificationCounts,
                        notificationPreviews = notificationPreviews,
                        musicSessions = musicSessions,
                        weatherTileState = weatherTileState,
                        photosTileState = photosTileState,
                        peopleTileState = peopleTileState,
                        wallpaperImage = wallpaperImage,
                        wallpaperRootSize = rootSize,
                        onRequestContacts = onRequestContacts,
                        onRequestPhotos = onRequestPhotos,
                        accentColor = accentColor,
                        startStyle = startStyle,
                        onOpenLauncherSettings = openLauncherSettings,
                        onTogglePinnedApp = onTogglePinnedApp,
                        onMovePinnedApp = onMovePinnedApp,
                        onResizePinnedApp = onResizePinnedApp,
                        onLaunch = launchFromStart,
                        onShowAllApps = { scope.launch { pagerState.animateScrollToPage(1) } },
                        hazeState = hazeState.takeIf { glassTiles },
                        modifier = Modifier.fillMaxSize().launcherSystemPadding(scrollBehindStatusBar = true)
                    )
                    }
                } else {
                    Box(Modifier.fillMaxSize().clipToBounds().graphicsLayer {
                        alpha = if (pagerState.currentPage == 0 && MetroSheet.progress() != 1f) 0f else 1f
                    }) {
                    AllAppsScreen(
                        backgroundShade = wallpaperShadeState,
                        onJumpListChange = { appsJumping = it },
                        wallpaperImage = wallpaperImage,
                        wallpaperRootSize = rootSize,
                        apps = apps,
                        appsLoading = appsLoading,
                        pinnedAppIds = pinnedAppIds,
                        accentColor = accentColor,
                        startStyle = startStyle,
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onTogglePinnedApp = { app ->
                            val wasPinned = app.pinKey() in pinnedAppIds
                            onTogglePinnedApp(app)
                            // Pinning sends you back to Start to see the new tile.
                            if (!wasPinned) scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        homeEntryCount = homeEntryCount + settingsReturnCount,
                        isExiting = startExiting && pagerState.currentPage == 1,
                        onLaunch = launchFromStart,
                        onUninstallApp = onUninstallApp,
                        // Edge-to-edge like Start: only side insets here; the top
                        // (SpartanBar or status bar) and bottom (nav bar) are
                        // applied inside AllAppsScreen so the list scrolls behind them.
                        modifier = Modifier.fillMaxSize().windowInsetsPadding(
                            androidx.compose.foundation.layout.WindowInsets.safeDrawing
                                .only(androidx.compose.foundation.layout.WindowInsetsSides.Horizontal))
                    )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartTiles(
    onEditingChange: (Boolean) -> Unit,
    liveTilesActive: Boolean,
    homeEntryCount: Int,
    isExiting: Boolean,
    apps: List<LauncherApp>,
    appsLoading: Boolean,
    pinnedAppIds: Set<String>,
    pinnedOrder: List<String>,
    tileSizes: Map<String, TileSize>,
    notificationCounts: Map<String, Int>,
    notificationPreviews: Map<String, TileNotificationPreview>,
    musicSessions: Map<String, MusicTileState>,
    weatherTileState: WeatherTileState,
    photosTileState: PhotosTileState,
    peopleTileState: PeopleTileState,
    wallpaperImage: ImageBitmap?,
    wallpaperRootSize: IntSize,
    onRequestContacts: () -> Unit,
    onRequestPhotos: () -> Unit,
    accentColor: Color,
    startStyle: StartStyle,
    onOpenLauncherSettings: () -> Unit,
    onTogglePinnedApp: (LauncherApp) -> Unit,
    onMovePinnedApp: (LauncherApp, Int) -> Unit,
    onResizePinnedApp: (LauncherApp, TileSize) -> Unit,
    onLaunch: LaunchHandler,
    onShowAllApps: () -> Unit = {},
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val folderContext = androidx.compose.ui.platform.LocalContext.current
    val storedFolders = StartFolderStore.folders
    val startScrollState = rememberScrollState()
    val topSpacing = 10.dp + launcherStartTopSpacing()
    var expandedFolderId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingFolderId by rememberSaveable { mutableStateOf<String?>(null) }
    var renameFolderId by remember { mutableStateOf<String?>(null) }
    var showFolderEditor by rememberSaveable { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    var folderDropTarget by remember { mutableStateOf<String?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var dragVisualOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var dragStartPosition by remember { mutableStateOf(Offset.Zero) }
    var dragTilt by remember { mutableStateOf(Offset.Zero) }
    val haptics = LocalHapticFeedback.current
    val currentEditing by rememberUpdatedState(editing)
    val currentEditingCallback by rememberUpdatedState(onEditingChange)
    LaunchedEffect(editing) { currentEditingCallback(editing) }
    LaunchedEffect(homeEntryCount) {
        editing = false
        selectedKey = null
        isDragging = false
        dragVisualOffset = Offset.Zero
        dragTilt = Offset.Zero
    }
    BackHandler(enabled = expandedFolderId != null && !editing) { expandedFolderId = null }
    BackHandler(enabled = editing) {
        editing = false
        selectedKey = null
    }
    val sheetMotion = rememberMetroMotion(
        isExiting,
        ready = apps.isNotEmpty() && !appsLoading,
        entranceDelayMillis = 90
    )
    MetroSheet.progress = { sheetMotion.value }
    MetroSheet.exiting = isExiting
    LaunchedEffect(sheetMotion) {
        snapshotFlow { sheetMotion.value != 1f }.collect { MetroSheet.animating = it }
    }
    var launchPivot by remember { mutableStateOf(Offset(0.5f, 0.4f)) }
    // Window position of the tile grid, so a tap can hand the launched app
    // the exact rectangle of its tile to grow out of.
    var tileGridWindowPos by remember { mutableStateOf(Offset.Zero) }
    // Tiles run almost edge to edge in both Start styles.
    val startSidePadding = 8.dp
    val endSidePadding = 8.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .metroSwivel(launchPivot, tilesAnimate = true) { sheetMotion.value }
            .pointerInput(Unit) {
                detectTapGestures {
                    if (currentEditing) {
                        editing = false
                        selectedKey = null
                    }
                }
            }
            .verticalScroll(startScrollState, enabled = !isDragging)
            .padding(start = startSidePadding, end = endSidePadding, top = topSpacing, bottom = 32.dp)
    ) {
        // Windows 10 Mobile tiles sit ~4dp apart over the wallpaper.
        val gap = 4.dp
        val pageWidthDp = maxWidth
        val pageHeightDp = maxHeight
        val tileWidth = (maxWidth - gap * (START_GRID_COLUMNS - 1)) / START_GRID_COLUMNS
        val activeFolders = remember(storedFolders, apps, pinnedOrder) {
            visibleStartFolders(storedFolders, pinnedOrder.filter { key -> apps.any { it.pinKey() == key } })
        }
        val pinnedApps = remember(apps, pinnedAppIds, pinnedOrder, activeFolders) {
            val defaults = defaultPinnedApps(apps)
            val defaultKeys = defaults.mapTo(mutableSetOf()) { (app, _) -> app.pinKey() }
            val byKey = (defaults + apps
                .filter { it.pinKey() in pinnedAppIds && it.pinKey() !in defaultKeys }
                .map { it to tileKindForApp(it) })
                .associateBy { it.first.pinKey() }
            pinnedOrder.mapNotNull { key ->
                val folder = activeFolders.firstOrNull { key in it.members }
                if (folder != null && folder.members.first() != key) null else
                    byKey[key]?.let { (app, kind) ->
                        if (folder != null) app.copy(label = folder.name) to TileKind.Apps
                        else app to if (key == BUILTIN_WEATHER_KEY) TileKind.Weather else kind
                    }
            }
        }
        if (showFolderEditor) {
            val folder = storedFolders.firstOrNull { it.id == editingFolderId }
            val assignedElsewhere = activeFolders.filter { it.id != editingFolderId }.flatMap { it.members }.toSet()
            val choices = pinnedOrder.mapNotNull { key ->
                apps.firstOrNull { it.pinKey() == key && key !in assignedElsewhere }
                    ?.let { FolderAppChoice(key, it.label) }
            }
            StartFolderEditor(folder, choices, selectedKey?.takeIf { key -> choices.any { it.key == key } },
                accentColor, onDismiss = { showFolderEditor = false },
                onSave = { name, members ->
                    val updated = StartFolder(folder?.id ?: java.util.UUID.randomUUID().toString(), name, members, folder?.size ?: "Medium")
                    StartFolderStore.save(folderContext, storedFolders.filterNot { it.id == updated.id } + updated)
                    showFolderEditor = false
                    editing = false
                    selectedKey = null
                }, onDissolve = {
                    StartFolderStore.save(folderContext, storedFolders.filterNot { it.id == editingFolderId })
                    expandedFolderId = null
                    showFolderEditor = false
                    editing = false
                    selectedKey = null
                })
        }
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            // No "edit folder" button in edit mode: rename inline inside the open folder.
            if (pinnedApps.isNotEmpty()) {
                val density = androidx.compose.ui.platform.LocalDensity.current
                val dragEdgePx = with(density) { 72.dp.toPx() }
                val gapPx = with(density) { gap.roundToPx() }
                val cellPx = with(density) { tileWidth.roundToPx() }
                val cellStepPx = cellPx + gapPx
                val currentMoveCallback by rememberUpdatedState(onMovePinnedApp)
                val currentPinOrder by rememberUpdatedState(pinnedOrder)
                val currentFolders by rememberUpdatedState(activeFolders)
                val placements = calculateTilePlacements(
                    pinnedApps.map { (app, kind) ->
                        activeFolders.firstOrNull { it.members.first() == app.pinKey() }
                            ?.let { folder -> TileSize.entries.firstOrNull { it.name == folder.size } ?: TileSize.Medium }
                            ?: tileSizes[app.pinKey()]
                            ?: if (app.packageName == PinnedShortcutStore.PACKAGE) TileSize.Medium
                            else when (kind) {
                                TileKind.Calendar, TileKind.Clock, TileKind.Weather, TileKind.People,
                                TileKind.Photos -> TileSize.Medium
                                else -> TileSize.Small
                            }
                    }
                )
                val expandedFolder = activeFolders.firstOrNull { it.id == expandedFolderId }
                val folderIndex = pinnedApps.indexOfFirst { it.first.pinKey() == expandedFolder?.members?.firstOrNull() }
                val expansionRow = if (folderIndex < 0) Int.MAX_VALUE else folderExpansionRow(
                    placements[folderIndex].row + placements[folderIndex].rows,
                    placements.map { it.row to it.rows }
                )
                val folderChildren = expandedFolder?.members.orEmpty().mapNotNull { key -> apps.firstOrNull { it.pinKey() == key } }
                val folderChildSizes = folderChildren.map { child ->
                    tileSizes[child.pinKey()] ?: when (tileKindForApp(child)) {
                        TileKind.Calendar, TileKind.Clock, TileKind.Weather, TileKind.People, TileKind.Photos -> TileSize.Medium
                        else -> TileSize.Small
                    }
                }
                val folderSpans = folderChildSizes.map { it.columns to it.rows }
                val folderRows = folderGridCells(folderSpans).maxOfOrNull { it.row + it.rows } ?: 0
                val panelHeight = 88.dp + tileWidth * folderRows + gap * (folderRows - 1).coerceAtLeast(0)
                val expandedHeightPx = if (expandedFolder == null) 0 else with(density) { panelHeight.roundToPx() } + gapPx
                LaunchedEffect(expandedFolderId) {
                    if (folderIndex >= 0) {
                        delay(250L)
                        startScrollState.animateScrollTo((placements[folderIndex].row * cellStepPx)
                            .coerceIn(0, startScrollState.maxValue))
                    }
                }
                val currentPinnedApps by rememberUpdatedState(pinnedApps)
                val currentPlacements by rememberUpdatedState(placements)
                val animatedTileSizes = pinnedApps.mapIndexed { index, (app, _) ->
                    androidx.compose.runtime.key(app.pinKey()) {
                        val placement = placements[index]
                        val width by animateFloatAsState(
                            (placement.columns * cellPx + (placement.columns - 1) * gapPx).toFloat(),
                            tween(240, easing = MetroEnterEasing),
                            label = "tile-resize-width"
                        )
                        val height by animateFloatAsState(
                            (placement.rows * cellPx + (placement.rows - 1) * gapPx).toFloat(),
                            tween(240, easing = MetroEnterEasing),
                            label = "tile-resize-height"
                        )
                        IntSize(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1))
                    }
                }
                Layout(
                    modifier = Modifier.onGloballyPositioned { tileGridWindowPos = it.positionInWindow() },
                    content = {
                        pinnedApps.forEachIndexed { index, (app, kind) ->
                            val key = app.pinKey()
                            androidx.compose.runtime.key(key) {
                            val folder = activeFolders.firstOrNull { it.members.first() == key }
                            val size = folder?.let { folder -> TileSize.entries.firstOrNull { it.name == folder.size } ?: TileSize.Medium }
                                ?: tileSizes[key] ?: when (kind) {
                                TileKind.Calendar, TileKind.Clock, TileKind.Weather, TileKind.People,
                                TileKind.Photos -> TileSize.Medium
                                else -> TileSize.Small
                            }
                            val placement = placements[index]
                            val targetX = placement.column * cellStepPx
                            val targetY = placement.row * cellStepPx + if (placement.row >= expansionRow) expandedHeightPx else 0
                            val tileWidthPx = placement.columns * cellPx + (placement.columns - 1) * gapPx
                            val tileHeightPx = placement.rows * cellPx + (placement.rows - 1) * gapPx
                            val moveSpring: androidx.compose.animation.core.AnimationSpec<Float> = if (isDragging) spring(
                                dampingRatio = 0.9f,
                                stiffness = Spring.StiffnessMedium
                            ) else spring(
                                dampingRatio = 0.78f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                            val animatedX by animateFloatAsState(
                                targetValue = targetX.toFloat(),
                                animationSpec = moveSpring,
                                label = "tile-x"
                            )
                            val animatedY by animateFloatAsState(
                                targetValue = targetY.toFloat(),
                                animationSpec = moveSpring,
                                label = "tile-y"
                            )
                            val isSelected = editing && selectedKey == key
                            val editScale by animateFloatAsState(
                                when {
                                    isSelected && isDragging -> 1.08f
                                    isSelected -> 1.02f
                                    editing -> 0.92f
                                    else -> 1f
                                },
                                // A soft spring settle when edit mode opens
                                // or the selection moves, not a flat tween.
                                spring(dampingRatio = 0.8f, stiffness = 220f),
                                label = "tile-edit-scale"
                            )
                            val editAlpha by animateFloatAsState(
                                if (editing && !isSelected) 0.5f else 1f,
                                tween(360, easing = MetroEnterEasing),
                                label = "tile-edit-focus"
                            )
                            val dragTranslationX by animateFloatAsState(
                                if (isSelected && isDragging) {
                                    dragStartPosition.x + dragVisualOffset.x - animatedX
                                } else 0f,
                                if (isDragging) tween(0) else spring(
                                    dampingRatio = 0.85f,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "tile-drop-x"
                            )
                            val dragTranslationY by animateFloatAsState(
                                if (isSelected && isDragging) {
                                    dragStartPosition.y + dragVisualOffset.y - animatedY
                                } else 0f,
                                if (isDragging) tween(0) else spring(
                                    dampingRatio = 0.85f,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "tile-drop-y"
                            )
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (animatedX - targetX).roundToInt(),
                                            (animatedY - targetY).roundToInt()
                                        )
                                    }
                                    .then(
                                        if (editing || isDragging) {
                                            Modifier.graphicsLayer {
                                                cameraDistance = 24f * density.density
                                                transformOrigin = TransformOrigin.Center
                                                scaleX = editScale
                                                scaleY = editScale
                                                // Keep protruding edit controls visible while focus alpha animates.
                                                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.ModulateAlpha
                                                alpha = editAlpha
                                                if (isSelected) {
                                                    translationX = dragTranslationX
                                                    translationY = dragTranslationY

                                                    rotationX = if (isDragging && tileHeightPx > 0) {
                                                        (-dragTilt.y / tileHeightPx * 22f).coerceIn(-8f, 8f)
                                                    } else {
                                                        0f
                                                    }
                                                    rotationY = if (isDragging && tileWidthPx > 0) {
                                                        (dragTilt.x / tileWidthPx * 22f).coerceIn(-8f, 8f)
                                                    } else {
                                                        0f
                                                    }
                                                }
                                            }
                                        } else Modifier
                                    )
                                    // No drop shadow: under see-through tiles it shows as a dark smudge.
                                    .shadow(
                                        elevation = 0.dp,
                                        // The corner buttons intentionally extend beyond the tile.
                                        clip = false
                                    )
                                    .zIndex(if (isSelected) 2f else 0f)
                                    .then(if (folderDropTarget == key) Modifier.border(3.dp, accentColor) else Modifier)
                                    .then(
                                        if (isSelected && !isDragging) Modifier.tileWiggle(index)
                                        else Modifier
                                    )
                                    .pointerInput(key, cellStepPx) {
                                        var lastHoveredKey: String? = null
                                        var hoverStartedAt = 0L
                                        detectTileDrag(
                                            editMode = { currentEditing },
                                            onDragStart = {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                expandedFolderId = null
                                                editing = true
                                                selectedKey = key
                                                isDragging = true
                                                dragVisualOffset = Offset.Zero
                                                dragTilt = Offset.Zero
                                                dragStartPosition = Offset(animatedX, animatedY)
                                                lastHoveredKey = null
                                                folderDropTarget = null
                                            },
                                            onDragEnd = {
                                                val target = folderDropTarget
                                                if (target != null && target != key) {
                                                    val newFolderId = java.util.UUID.randomUUID().toString()
                                                    val updated = mergeStartFolders(currentFolders, key, target, newFolderId)
                                                    StartFolderStore.save(folderContext, updated)
                                                    expandedFolderId = updated.firstOrNull { target in it.members }?.id
                                                    // A brand-new folder opens with its name ready to type, like W10M.
                                                    if (expandedFolderId == newFolderId) renameFolderId = newFolderId
                                                    editing = false
                                                    selectedKey = null
                                                }
                                                folderDropTarget = null
                                                isDragging = false
                                                dragVisualOffset = Offset.Zero
                                                dragTilt = Offset.Zero
                                            },
                                            onDragCancel = {
                                                folderDropTarget = null
                                                isDragging = false
                                                dragVisualOffset = Offset.Zero
                                                dragTilt = Offset.Zero
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                dragTilt = Offset(
                                                    dragTilt.x * 0.62f + amount.x * 0.38f,
                                                    dragTilt.y * 0.62f + amount.y * 0.38f
                                                )
                                                dragVisualOffset += amount
                                                val pointerY = dragStartPosition.y + dragVisualOffset.y + tileHeightPx / 2f
                                                val viewTop = startScrollState.value.toFloat()
                                                val viewBottom = viewTop + startScrollState.viewportSize
                                                val intoTop = dragEdgePx - (pointerY - viewTop)
                                                val intoBottom = dragEdgePx - (viewBottom - pointerY)
                                                val scrollBy = when {
                                                    intoTop > 0f -> -intoTop.coerceAtMost(36f)
                                                    intoBottom > 0f -> intoBottom.coerceAtMost(36f)
                                                    else -> 0f
                                                }
                                                if (scrollBy != 0f) {
                                                    dragVisualOffset += Offset(0f, startScrollState.dispatchRawDelta(scrollBy))
                                                }
                                                val centerX = dragStartPosition.x + dragVisualOffset.x + tileWidthPx / 2f
                                                val centerY = dragStartPosition.y + dragVisualOffset.y + tileHeightPx / 2f
                                                val hovered = currentPlacements.indexOfFirst { target ->
                                                    val left = target.column * cellStepPx
                                                    val top = target.row * cellStepPx
                                                    val width = target.columns * cellPx + (target.columns - 1) * gapPx
                                                    val height = target.rows * cellPx + (target.rows - 1) * gapPx
                                                    centerX in (left + width * 0.2f)..(left + width * 0.8f) &&
                                                        centerY in (top + height * 0.2f)..(top + height * 0.8f)
                                                }
                                                val hoveredKey = currentPinnedApps.getOrNull(hovered)?.first?.pinKey()
                                                val from = currentPinnedApps.indexOfFirst { it.first.pinKey() == key }
                                                val targetPlacement = currentPlacements.getOrNull(hovered)
                                                val overCenter = targetPlacement != null && hoveredKey != key &&
                                                    centerX in ((targetPlacement.column + targetPlacement.columns * .3f) * cellStepPx)..((targetPlacement.column + targetPlacement.columns * .7f) * cellStepPx) &&
                                                    centerY in ((targetPlacement.row + targetPlacement.rows * .3f) * cellStepPx)..((targetPlacement.row + targetPlacement.rows * .7f) * cellStepPx)
                                                folderDropTarget = hoveredKey.takeIf { overCenter }
                                                if (overCenter) {
                                                    lastHoveredKey = null
                                                } else if (hoveredKey != null && hoveredKey != key && from >= 0) {
                                                    val now = android.os.SystemClock.uptimeMillis()
                                                    if (hoveredKey != lastHoveredKey) {
                                                        lastHoveredKey = hoveredKey
                                                        hoverStartedAt = now
                                                    } else if (now - hoverStartedAt >= 110L) {
                                                        val destination = currentPinOrder.indexOf(hoveredKey)
                                                        val source = currentPinOrder.indexOf(key)
                                                        if (destination >= 0 && source >= 0) {
                                                            currentMoveCallback(app, destination - source)
                                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        }
                                                        hoverStartedAt = now
                                                    }
                                                } else if (hoveredKey == null) {
                                                    lastHoveredKey = null
                                                }
                                            }
                                        )
                                    }
                            ) {
                                androidx.compose.foundation.layout.BoxWithConstraints(
                                    Modifier.fillMaxSize()
                                ) {
                                    LauncherTile(
                                        animationsActive = liveTilesActive && !editing && !isDragging && !isExiting,
                                        app = app,
                                        kind = kind,
                                        width = maxWidth,
                                        height = maxHeight,
                                        notificationCount = if (folder != null) 0 else notificationCounts[app.packageName] ?: 0,
                                        notificationPreview = if (folder != null) null else notificationPreviews[app.packageName],
                                        musicSessions = musicSessions,
                                        weatherTileState = weatherTileState,
                                        photosTileState = photosTileState,
                                        peopleTileState = peopleTileState,
                                        wallpaperImage = wallpaperImage,
                                        wallpaperRootSize = wallpaperRootSize,
                                        accentColor = accentColor,
                                        startStyle = startStyle,
                                        hazeState = hazeState,
                                        tileSize = size,
                                        showLabel = size != TileSize.Small,
                                        editing = isSelected,
                                        showEditControls = isSelected && !isDragging,
                                        // Corner buttons stick out 24dp; at the
                                        // right screen edge they'd be cut off,
                                        // so those tiles wear them on the left,
                                        // and full-width tiles just inside.
                                        editControlsOnStartSide =
                                            placement.column + placement.columns >= START_GRID_COLUMNS &&
                                                placement.column > 0,
                                        editControlsInsetEnd =
                                            placement.column + placement.columns >= START_GRID_COLUMNS &&
                                                placement.column == 0,
                                        onUnpin = {
                                            if (folder != null) {
                                                StartFolderStore.save(folderContext, storedFolders.filterNot { it.id == folder.id })
                                                expandedFolderId = null
                                            } else onTogglePinnedApp(app)
                                            selectedKey = pinnedApps.filterNot { it.first.pinKey() == key }
                                                .getOrNull(index.coerceAtMost(pinnedApps.lastIndex - 1).coerceAtLeast(0))
                                                ?.first?.pinKey()
                                            if (selectedKey == null) editing = false
                                        },
                                        folderContent = if (folder == null) null else ({
                                            if (expandedFolderId != folder.id) StartFolderMosaic(folder.members) { member, modifier ->
                                                apps.firstOrNull { it.pinKey() == member }?.let { child ->
                                                    val childKind = tileKindForApp(child)
                                                    // Folder mini-tiles follow the Start transparency so brand-colored apps don't sit opaque on the wallpaper.
                                                    Box(modifier.background((appBrandFor(child)?.color ?: accentColor).copy(alpha =
                                                        if (appBrandFor(child) != null) 1f
                                                        else if (wallpaperImage != null && startStyle == StartStyle.WindowsPhone81) 0.45f
                                                        else startStyle.tileAlpha)), contentAlignment = Alignment.Center) {
                                                        val photo = when (childKind) {
                                                            TileKind.Photos -> photosTileState.photos.firstOrNull()
                                                            TileKind.People -> peopleTileState.contacts.firstOrNull { it.photo != null }?.photo
                                                            else -> null
                                                        }
                                                        val glyph = Modifier.fillMaxSize(0.42f)
                                                        if (photo != null) Image(photo, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                                        else if (appBrandFor(child) != null || childKind == TileKind.Apps ||
                                                            (childKind != TileKind.Messages && IconPacks.resolved(child.packageName, child.activityName) != null))
                                                            LauncherAppIcon(child, glyph)
                                                        else MetroTileIcon(childKind, glyph)
                                                    }
                                                }
                                            }
                                        }),
                                        onResize = {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (folder != null) StartFolderStore.save(folderContext,
                                                storedFolders.map { if (it.id == folder.id) it.copy(size = size.next().name) else it })
                                            else onResizePinnedApp(app, size.next())
                                        },
                                        onClick = {
                                            if (!editing && folder == null) {
                                                // Zoom into the tapped tile, W10M style. The
                                                // pivot is a fraction of the whole page layer,
                                                // not just the tile grid, or tiles lurch.
                                                val layerWidthPx = with(density) { (pageWidthDp + startSidePadding + endSidePadding).toPx() }
                                                val layerHeightPx = with(density) { (pageHeightDp + topSpacing + 32.dp).toPx() }
                                                launchPivot = Offset(
                                                    ((with(density) { startSidePadding.toPx() } + targetX + tileWidthPx / 2f) / layerWidthPx).coerceIn(0f, 1f),
                                                    ((with(density) { topSpacing.toPx() } + targetY + tileHeightPx / 2f) / layerHeightPx).coerceIn(0f, 1f)
                                                )
                                            }
                                            if (editing) {
                                                selectedKey = key
                                            } else if (folder != null) {
                                                expandedFolderId = if (expandedFolderId == folder.id) null else folder.id
                                            } else if (kind == TileKind.People && !peopleTileState.permissionGranted) {
                                                onRequestContacts()
                                            } else if (kind == TileKind.Settings) {
                                                onOpenLauncherSettings()
                                            } else if (kind == TileKind.Photos) {
                                                onRequestPhotos()
                                            } else {
                                                onLaunch(app, android.graphics.Rect(
                                                    (tileGridWindowPos.x + targetX).roundToInt(),
                                                    (tileGridWindowPos.y + targetY).roundToInt(),
                                                    (tileGridWindowPos.x + targetX + tileWidthPx).roundToInt(),
                                                    (tileGridWindowPos.y + targetY + tileHeightPx).roundToInt()
                                                ))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        }
                    if (expandedFolder != null) {
                        StartFolderPanel(expandedFolder.name, folderChildren.size,
                            onEdit = { editingFolderId = expandedFolder.id; showFolderEditor = true },
                            onClose = { expandedFolderId = null }, accent = accentColor,
                            sizes = folderSpans,
                            onRename = { newName ->
                                val current = StartFolderStore.folders
                                StartFolderStore.save(folderContext, current.map { if (it.id == expandedFolder.id) it.copy(name = newName) else it })
                            },
                            startRenaming = renameFolderId == expandedFolder.id,
                            onRenamingStarted = { renameFolderId = null }) { index, modifier ->
                            val child = folderChildren[index]
                            val childKind = tileKindForApp(child)
                            BoxWithConstraints(modifier) {
                                LauncherTile(
                                    animationsActive = liveTilesActive && !editing && !isExiting,
                                    app = child, kind = childKind, width = maxWidth, height = maxHeight,
                                    notificationCount = notificationCounts[child.packageName] ?: 0,
                                    notificationPreview = notificationPreviews[child.packageName],
                                    musicSessions = musicSessions, weatherTileState = weatherTileState,
                                    photosTileState = photosTileState, peopleTileState = peopleTileState,
                                    wallpaperImage = wallpaperImage, wallpaperRootSize = wallpaperRootSize,
                                    accentColor = accentColor, startStyle = startStyle, hazeState = hazeState,
                                    tileSize = folderChildSizes[index], showLabel = folderChildSizes[index] != TileSize.Small,
                                    editing = editing,
                                    showEditControls = editing,
                                    onUnpin = {
                                        val remaining = expandedFolder.members.filter { it != child.pinKey() }
                                        StartFolderStore.save(folderContext, if (remaining.size < 2) {
                                            storedFolders.filterNot { it.id == expandedFolder.id }
                                        } else {
                                            storedFolders.map { if (it.id == expandedFolder.id) it.copy(members = remaining) else it }
                                        })
                                        if (remaining.size < 2) expandedFolderId = null
                                    },
                                    onResize = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onResizePinnedApp(child, folderChildSizes[index].next())
                                    },
                                    onClick = {
                                        when {
                                            childKind == TileKind.People && !peopleTileState.permissionGranted -> onRequestContacts()
                                            childKind == TileKind.Photos -> onRequestPhotos()
                                            childKind == TileKind.Settings -> onOpenLauncherSettings()
                                            else -> onLaunch(child, null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    }
                ) { measurables, constraints ->
                    val placeables = measurables.take(pinnedApps.size).mapIndexed { index, measurable ->
                        measurable.measure(
                            Constraints.fixed(
                                animatedTileSizes[index].width,
                                animatedTileSizes[index].height
                            )
                        )
                    }
                    val folderPanel = if (expandedFolder != null) measurables.last().measure(
                        Constraints.fixed(constraints.maxWidth, (expandedHeightPx - gapPx).coerceAtLeast(1))
                    ) else null
                    val rowCount = placements.maxOfOrNull { it.row + it.rows } ?: 0
                    val targetHeight = if (rowCount == 0) 0 else rowCount * cellPx + (rowCount - 1) * gapPx
                    val animatedHeight = placeables.mapIndexed { index, placeable ->
                        placements[index].row * cellStepPx + placeable.height
                    }.maxOrNull() ?: 0
                    val layoutHeight = max(targetHeight, animatedHeight) + expandedHeightPx
                    layout(constraints.maxWidth, layoutHeight) {
                        placeables.forEachIndexed { index, placeable ->
                            val placement = placements[index]
                            placeable.place(
                                x = placement.column * (cellPx + gapPx),
                                y = placement.row * (cellPx + gapPx) + if (placement.row >= expansionRow) expandedHeightPx else 0
                            )
                        }
                        folderPanel?.place(0, expansionRow * cellStepPx)
                    }
                }
                // Windows 10 Mobile ends Start's tile list with an "All apps"
                // link with an arrow, right-aligned directly under the tiles;
                // it travels with the list as it grows. Opens the app list.
                if (!editing) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 4.dp)
                            .semantics { contentDescription = "All apps" }
                            .clickable { onShowAllApps() },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All apps",
                            color = Color.White,
                            fontFamily = MetroRegular,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.width(7.dp))
                        Icon(
                            painterResource(R.drawable.ic_mdl2_back),
                            contentDescription = null,
                            modifier = Modifier.size(17.dp).graphicsLayer { scaleX = -1f },
                            tint = Color.White
                        )
                    }
                }
            }
        }
        // Loading and empty states live outside the swivel page above, so
        // they stay put while the tile page waits invisible for its content.
        if (pinnedApps.isEmpty()) {
            if (appsLoading) {
                MetroAppsLoading(
                    accent = accentColor,
                    phoneDots = startStyle == StartStyle.WindowsPhone81,
                    modifier = Modifier.align(Alignment.Center).fillMaxSize()
                )
            } else {
                // Everything is loaded and there really is nothing pinned:
                // a quiet hint instead of the old error-looking message.
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Start is empty",
                        color = Color.White,
                        fontFamily = MetroRegular,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Long-press an app in All apps and choose Pin to Start.",
                        color = MetroTheme.colors.subtle,
                        fontFamily = MetroRegular,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTileDrag(
    editMode: () -> Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onDrag: (PointerInputChange, Offset) -> Unit
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val pointer = if (editMode()) {
            awaitTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                ?: return@awaitEachGesture
        } else {
            val released = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                var holding = true
                while (holding) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id }
                    holding = change != null && change.pressed &&
                        (change.position - down.position).getDistance() <= viewConfiguration.touchSlop
                }
                true
            } != null
            if (released) return@awaitEachGesture
            down
        }
        onDragStart()
        val completed = drag(pointer.id) { change ->
            onDrag(change, change.positionChange())
            change.consume()
        }
        if (completed) onDragEnd() else onDragCancel()
    }
}

private data class TilePlacement(
    val row: Int,
    val column: Int,
    val columns: Int,
    val rows: Int
)

private fun calculateTilePlacements(sizes: List<TileSize>): List<TilePlacement> {
    val occupied = mutableListOf<MutableList<Boolean>>()
    return sizes.map { tileSize ->
        var row = 0
        var column: Int
        while (true) {
            column = 0
            var found = false
            while (column + tileSize.columns <= START_GRID_COLUMNS) {
                while (occupied.size < row + tileSize.rows) {
                    occupied += MutableList(START_GRID_COLUMNS) { false }
                }
                val free = (row until row + tileSize.rows).all { r ->
                    (column until column + tileSize.columns).all { c -> !occupied[r][c] }
                }
                if (free) {
                    found = true
                    break
                }
                column++
            }
            if (found) break
            row++
        }
        for (r in row until row + tileSize.rows) {
            for (c in column until column + tileSize.columns) occupied[r][c] = true
        }
        TilePlacement(row, column, tileSize.columns, tileSize.rows)
    }
}

@Composable
private fun Modifier.tileWiggle(index: Int): Modifier {
    val transition = rememberInfiniteTransition(label = "tile-wiggle-$index")
    val rotation by transition.animateFloat(
        initialValue = -0.8f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tile-rotation"
    )
    return graphicsLayer { rotationZ = rotation }
}

@Composable
private fun LauncherTile(
    animationsActive: Boolean,
    app: LauncherApp,
    kind: TileKind,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    notificationCount: Int,
    notificationPreview: TileNotificationPreview?,
    musicSessions: Map<String, MusicTileState>,
    weatherTileState: WeatherTileState,
    photosTileState: PhotosTileState,
    peopleTileState: PeopleTileState,
    wallpaperImage: ImageBitmap?,
    wallpaperRootSize: IntSize,
    accentColor: Color,
    startStyle: StartStyle,
    hazeState: HazeState? = null,
    tileSize: TileSize,
    showLabel: Boolean,
    editing: Boolean = false,
    showEditControls: Boolean = editing,
    editControlsOnStartSide: Boolean = false,
    editControlsInsetEnd: Boolean = false,
    folderContent: (@Composable () -> Unit)? = null,
    onUnpin: () -> Unit = {},
    onResize: () -> Unit = {},
    onClick: () -> Unit
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(animationsActive, kind) {
        if (!animationsActive || (kind != TileKind.Clock && kind != TileKind.Calendar)) return@LaunchedEffect
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(60_000L - nowMillis % 60_000L)
        }
    }
    val musicState = musicSessions[app.packageName]
    val serinaTile = app.packageName == "com.example.serina"
    val pinnedShortcut = app.packageName == PinnedShortcutStore.PACKAGE
    // Pinned contacts keep their photo face in every state, like the reference
    // (edit mode just overlays the unpin/resize badges on the same face).
    val liveContent = pinnedShortcut ||
        (!editing && (tileSize != TileSize.Small || kind == TileKind.Calendar || serinaTile) &&
            (serinaTile || kind == TileKind.Clock || kind == TileKind.Calendar ||
                kind == TileKind.Weather || kind == TileKind.People || kind == TileKind.Photos ||
                (kind == TileKind.Music && musicState != null)))
    val notificationFaceAvailable = !editing && tileSize != TileSize.Small &&
        notificationPreview != null && !(kind == TileKind.Music && musicState != null)
    val flipProgress = rememberNotificationTileFlip(
        key = app.pinKey(),
        preview = notificationPreview.takeIf { notificationFaceAvailable },
        active = animationsActive
    )
    val showLiveContent = liveContent
    val coversOwnFace = liveContent &&
        (kind == TileKind.Photos || kind == TileKind.Weather || kind == TileKind.People)
    val peopleShowsWallpaper = liveContent && kind == TileKind.People && wallpaperImage != null
    val tileColor = if (folderContent != null) accentColor else appBrandFor(app)?.color ?: accentColor
    val isBrandTile = folderContent == null && appBrandFor(app) != null
    // Circles paint themselves. The glass plate is still the tile background when blur is on.
    val glass = hazeState?.takeIf { (!coversOwnFace || kind == TileKind.People) && !isBrandTile }
    val dateTime = remember(nowMillis) {
        Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault())
    }
    val timeText = remember(dateTime) {
        DateTimeFormatter.ofLocalizedTime(java.time.format.FormatStyle.SHORT).format(dateTime)
    }
    val dayText = remember(dateTime) {
        DateTimeFormatter.ofPattern("EEEE", Locale.getDefault()).format(dateTime)
    }
    val dateText = remember(dateTime) {
        DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault()).format(dateTime)
    }

    var tilePositionInRoot by remember(app.pinKey()) {
        mutableStateOf(androidx.compose.ui.geometry.Offset.Zero)
    }
    // Windows Phone tiles tilt toward the finger while pressed.
    var pressedAt by remember { mutableStateOf<Offset?>(null) }
    var pressedSize by remember { mutableStateOf(IntSize.Zero) }
    val pressFraction = pressedAt?.let { at ->
        if (pressedSize.width > 0 && pressedSize.height > 0) {
            Offset(
                (at.x / pressedSize.width).coerceIn(0f, 1f),
                (at.y / pressedSize.height).coerceIn(0f, 1f)
            )
        } else null
    }
    // Press feel, Windows 10 Mobile style: the tile leans toward your finger
    // and sinks a touch while held, then springs back on release. Springs
    // both ways so pressing in is instant and letting go has a little life.
    val pressSpec = spring<Float>(dampingRatio = 0.62f, stiffness = 520f)
    val pressRotationY by animateFloatAsState(
        if (!editing && pressFraction != null) (pressFraction.x - 0.5f) * 20f else 0f,
        pressSpec, label = "tile-press-y"
    )
    val pressRotationX by animateFloatAsState(
        if (!editing && pressFraction != null) (0.5f - pressFraction.y) * 20f else 0f,
        pressSpec, label = "tile-press-x"
    )
    val pressScale by animateFloatAsState(
        if (!editing && pressFraction != null) 0.95f else 1f,
        pressSpec, label = "tile-press-scale"
    )
    val trackWallpaper = startStyle == StartStyle.WindowsPhone81 && wallpaperImage != null
    val tileKey = app.pinKey()
    var tileWindowPos by remember { mutableStateOf(Offset.Zero) }
    val screenHeightPx = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp *
        androidx.compose.ui.platform.LocalDensity.current.density
    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                pressedSize = it.size
                tileWindowPos = it.positionInWindow()
            }
            .graphicsLayer {
                // W10M unlock entrance: each tile swings in on its left
                // edge, staggered top to bottom and a touch left to right.
                val u = MetroSheet.unlock.value
                if (u < 1f && screenHeightPx > 0f) {
                    val rowF = (tileWindowPos.y / screenHeightPx).coerceIn(0f, 1f)
                    val colF = (tileWindowPos.x / (screenHeightPx * 0.46f)).coerceIn(0f, 1f)
                    val start = rowF * 0.42f + colF * 0.12f
                    val l = MetroEnterEasing.transform(((u - start) / 0.46f).coerceIn(0f, 1f))
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    cameraDistance = 10f * density
                    rotationY = -80f * (1f - l)
                    alpha = (l * 1.6f).coerceIn(0f, 1f)
                }
            }
            .graphicsLayer {
                // W10M dive, per tile: rows fade out top first as the
                // sheet zooms; the tapped tile goes last.
                // Tapped tile is the focal point: it stays crisp and leads
                // the dive slightly ahead; the rest fade by row with a
                // slight perspective push away from it. The return replays
                // the push, settling every tile at once.
                // Pre-warm: keep each tile's offscreen layer allocated at
                // rest so the first dive frames don't pay for creating them.
                // An offscreen layer clips to the tile, which cropped the edit-mode
                // corner buttons; skip it while editing (nothing dives then).
                compositingStrategy = if (editing) androidx.compose.ui.graphics.CompositingStrategy.Auto
                    else androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                val v = MetroSheet.progress()
                if (v > 1f && screenHeightPx > 0f && pressedSize.width > 0) {
                    val center = tileWindowPos + Offset(pressedSize.width / 2f, pressedSize.height / 2f)
                    val screenW = screenHeightPx * 0.46f
                    val dx = ((center.x - MetroSheet.tappedCenter.x) / screenW).coerceIn(-1.5f, 1.5f)
                    val dy = ((center.y - MetroSheet.tappedCenter.y) / screenHeightPx).coerceIn(-1f, 1f)
                    val tapped = MetroSheet.tappedKey == tileKey
                    val local = if (MetroSheet.exiting) {
                        val rowF = (center.y / screenHeightPx).coerceIn(0f, 1f)
                        val start = if (tapped) 0.6f else rowF * 0.25f
                        FastOutSlowInEasing.transform(((v - 1f - start) / (if (tapped) 0.3f else 0.5f)).coerceIn(0f, 1f))
                    } else FastOutSlowInEasing.transform(((v - 1f) / 0.35f).coerceIn(0f, 1f))
                    val travel = FastOutSlowInEasing.transform((v - 1f).coerceIn(0f, 1f))
                    cameraDistance = 9f * density
                    if (tapped && MetroSheet.exiting) {
                        val lead = 1f + 0.12f * travel
                        scaleX = lead; scaleY = lead
                    } else {
                        val push = 1f + 0.08f * local
                        scaleX = push; scaleY = push
                        rotationY = dx * 14f * local
                        rotationX = -dy * 18f * local
                        translationX = dx * 24f * density * local
                        translationY = dy * 24f * density * local
                    }
                    alpha = 1f - local
                }
            }
            .graphicsLayer {
                rotationX = pressRotationX
                rotationY = pressRotationY
                scaleX = pressScale
                scaleY = pressScale
                cameraDistance = 16f * density
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull()
                            ?: continue
                        pressedAt = if (change.pressed) change.position else null
                        MetroSheet.pressing = change.pressed
                    }
                }
            }
            .clickable(onClick = {
                MetroSheet.tappedKey = tileKey
                MetroSheet.tappedCenter = tileWindowPos + Offset(pressedSize.width / 2f, pressedSize.height / 2f)
                onClick()
            })
            .then(
                if (trackWallpaper) {
                    Modifier.onGloballyPositioned { tilePositionInRoot = it.positionInRoot() }
                } else Modifier
            )
    ) {
      if (glass != null) {
          // Cheap blur from finger-down through the dive/return: the
          // full-quality pass re-renders every frame while tiles move.
          Box(Modifier.fillMaxSize().windowsGlassTile(tileColor, glass,
              if (MetroSheet.frozen) dev.chrisbanes.haze.HazePerformanceMode.Performance else null))
      }
      Box(Modifier.fillMaxSize().clipToBounds()) {
        val tileWallpaper = wallpaperImage?.takeIf {
            startStyle == StartStyle.WindowsPhone81 && !isBrandTile &&
                !(liveContent && kind == TileKind.Photos)
        }
        val wallpaperInTile = tileWallpaper != null
        tileWallpaper?.let { image ->
            Canvas(Modifier.fillMaxSize()) {
                if (wallpaperRootSize.width > 0 && wallpaperRootSize.height > 0) {
                    val scale = max(
                        wallpaperRootSize.width.toFloat() / image.width,
                        wallpaperRootSize.height.toFloat() / image.height
                    )
                    val sourceWidth = size.width / scale
                    val sourceHeight = size.height / scale
                    val sourceLeft = (
                        (image.width - wallpaperRootSize.width / scale) / 2f +
                            tilePositionInRoot.x / scale
                        ).roundToInt().coerceIn(0, image.width - 1)
                    val sourceTop = (
                        (image.height - wallpaperRootSize.height / scale) / 2f +
                            tilePositionInRoot.y / scale
                        ).roundToInt().coerceIn(0, image.height - 1)
                    val sourceSize = IntSize(
                        sourceWidth.roundToInt().coerceAtLeast(1)
                            .coerceAtMost(image.width - sourceLeft),
                        sourceHeight.roundToInt().coerceAtLeast(1)
                            .coerceAtMost(image.height - sourceTop)
                    )
                    drawImage(
                        image = image,
                        srcOffset = IntOffset(sourceLeft, sourceTop),
                        srcSize = sourceSize,
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
                    )
                }
            }
            Box(
                Modifier.fillMaxSize().background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.55f),
                        0.65f to Color.Black.copy(alpha = 0.55f),
                        1f to Color.Black.copy(alpha = 0.72f)
                    )
                )
            )
        }
      Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = -flipProgress.value * size.height
            }
            .clipToBounds()
      ) {
        // Title size is ours, not the library tile's: its one-size formula
        // made medium-tile titles as big as large ones. Windows 10 Mobile
        // keeps them small — clear, but quiet.
        val titleSize = when (tileSize) {
            TileSize.Medium -> 13.sp
            TileSize.Wide -> 14.sp
            TileSize.Large -> 16.sp
            TileSize.Small -> 12.sp
        }
        Tile(
            label = "",
            color = if (wallpaperInTile || glass != null) Color.Transparent else
                tileColor.copy(alpha = brandTileAlpha(startStyle.tileAlpha, isBrandTile)),
            w = width,
            h = height,
            glyph = null
        ) {
            onClick()
        }
        // Phone tile shows the carrier at bottom-left, like W10M ("T-Mobile").
        val tileTitle = if (tileKindForApp(app) == TileKind.Phone) CarrierName.current ?: app.label else app.label
        if (showLabel && !showLiveContent && !wallpaperInTile) {
            Text(
                text = tileTitle,
                color = Color.White,
                fontFamily = MetroRegular,
                fontSize = titleSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            )
        }
        if (wallpaperInTile) {
            if (showLabel && !showLiveContent) {
                Text(
                    text = tileTitle,
                    color = Color.White,
                    fontFamily = MetroRegular,
                    fontSize = titleSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }
        }
        if (folderContent != null) {
            folderContent()
        } else if (liveContent && pinnedShortcut) {
            val tileContext = androidx.compose.ui.platform.LocalContext.current
            val (phone, email) = remember(app.pinKey()) {
                PinnedShortcutStore.detailsFor(tileContext, app.pinKey())
            }
            PinnedShortcutTileContent(
                app = app,
                accentColor = accentColor,
                tileSize = tileSize,
                animationsActive = animationsActive && !editing,
                phone = phone,
                email = email,
                modifier = Modifier.fillMaxSize()
            )
        } else if (liveContent && serinaTile) {
            SerinaTileContent(
                animationsActive = animationsActive,
                label = app.label.takeIf { showLabel && tileSize != TileSize.Small },
                modifier = Modifier.fillMaxSize()
            )
        } else if (liveContent && kind == TileKind.Photos) {
            PhotosTileContent(
                animationsActive = animationsActive,
                state = photosTileState,
                tileLabel = app.label,
                modifier = Modifier.fillMaxSize()
            )
        } else if (liveContent && kind == TileKind.People) {
            PeopleTileContent(
                animationsActive = animationsActive,
                state = peopleTileState,
                accentColor = accentColor,
                onWallpaper = peopleShowsWallpaper,
                insideBorder = glass != null,
                modifier = Modifier.fillMaxSize()
            )
        } else if (liveContent && kind == TileKind.Calendar) {
            CalendarTileContent(dateTime.toLocalDate(), app.label, Modifier.fillMaxSize(), animationsActive)
        } else if (liveContent && kind == TileKind.Weather) {
            WeatherTileContent(weatherTileState, Modifier.fillMaxSize(), animationsActive)
        } else if (liveContent && kind == TileKind.Music) {
            MusicTileContent(
                state = requireNotNull(musicState),
                tileLabel = app.label,
                animationsActive = animationsActive,
                classic = startStyle == StartStyle.WindowsPhone81,
                modifier = Modifier.fillMaxSize()
            )
        } else if (liveContent) {
            Column(
                modifier = Modifier
                    .align(if (kind == TileKind.Clock && tileSize == TileSize.Medium) Alignment.Center else Alignment.CenterStart)
                    .padding(horizontal = if (kind == TileKind.Clock && tileSize == TileSize.Medium) 4.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                when (kind) {
                    TileKind.Weather -> Unit
                    TileKind.Photos -> Unit
                    TileKind.People -> Unit
                    TileKind.Clock -> {
                    val timeOnly = tileSize == TileSize.Medium
                    AnimatedTileText(
                        text = timeText,
                        fontSize = if (timeOnly) (width.value * 0.22f).coerceIn(22f, 30f).sp else 34.sp,
                        maxLines = 1
                    )
                    if (!timeOnly) Text(
                        text = "$dayText, $dateText",
                        color = Color.White,
                        fontFamily = MetroRegular,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    }
                    TileKind.Calendar -> Unit
                    else -> Unit
                }
            }
        } else {
            val iconSize = if (kind == TileKind.Messages) {
                when (tileSize) {
                    TileSize.Small -> 44.dp
                    TileSize.Medium -> if (notificationCount >= 10) 50.dp else 68.dp
                    TileSize.Wide -> 76.dp
                    TileSize.Large -> 96.dp
                }
            } else {
                when (tileSize) {
                    TileSize.Small -> 28.dp
                    TileSize.Medium -> 42.dp
                    TileSize.Wide -> 48.dp
                    TileSize.Large -> 64.dp
                }
            }
            // WP8.1/W10M: icon shifts left with the count to its right, the pair centred in the tile.
            val showLargeNotificationCount = notificationCount > 0
            val rowIconSize = if (tileSize == TileSize.Small) (if (kind == TileKind.Messages) 26.dp else 22.dp) else iconSize
            if (showLargeNotificationCount) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (tileSize == TileSize.Small) 3.dp else 8.dp)
                ) {
                    if (kind != TileKind.Messages && IconPacks.resolved(app.packageName, app.activityName) != null) {
                        LauncherAppIcon(app, Modifier.size(rowIconSize))
                    } else if (kind == TileKind.Music) {
                        MetroMusicGlyph(Modifier.size(rowIconSize), classic = startStyle == StartStyle.WindowsPhone81)
                    } else if (kind == TileKind.Messages) {
                        MessagingTileIcon(notificationCount, Modifier.size(rowIconSize))
                    } else if (appBrandFor(app) != null || kind == TileKind.Apps) {
                        LauncherAppIcon(app, Modifier.size(rowIconSize))
                    } else {
                        MetroTileIcon(kind, Modifier.size(rowIconSize))
                    }
                    AnimatedContent(
                        targetState = notificationCount,
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn()) togetherWith
                                (slideOutVertically { -it } + fadeOut())
                        },
                        label = "tile-notification-count"
                    ) { count ->
                        Text(
                            text = if (count > 99) "99+" else count.toString(),
                            color = Color.White,
                            fontFamily = MetroRegular,
                            fontSize = when (tileSize) {
                                TileSize.Medium -> 34.sp
                                TileSize.Wide -> 40.sp
                                TileSize.Large -> 48.sp
                                TileSize.Small -> if (count > 99) 17.sp else 23.sp
                            },
                            maxLines = 1,
                            lineHeight = 26.sp,
                            modifier = if (kind == TileKind.Messages && count < 10 && tileSize != TileSize.Small)
                                Modifier.offset(x = (-10).dp) else Modifier
                        )
                    }
                }
            } else {
                // Small tiles get a larger icon, unless a notification badge has
                // claimed the corner — that layout keeps its current size.
                val displayIconSize = if (tileSize == TileSize.Small && kind != TileKind.Messages) 36.dp else iconSize
                val iconModifier = Modifier
                    .size(displayIconSize)
                    .align(
                        Alignment.Center
                    )
                    .then(
                        Modifier
                    )
                if (kind != TileKind.Messages && IconPacks.resolved(app.packageName, app.activityName) != null) {
                    LauncherAppIcon(app, iconModifier)
                } else if (kind == TileKind.Music) {
                    MetroMusicGlyph(iconModifier, classic = startStyle == StartStyle.WindowsPhone81)
                } else if (kind == TileKind.Messages) {
                    MessagingTileIcon(notificationCount, iconModifier)
                } else if (appBrandFor(app) != null || kind == TileKind.Apps) {
                    LauncherAppIcon(app, iconModifier)
                } else {
                    MetroTileIcon(
                        kind = kind,
                        modifier = iconModifier
                    )
                }
            }
        }
      }
      if (notificationFaceAvailable) {
          NotificationTileContent(
              preview = requireNotNull(notificationPreview),
              label = app.label,
              color = if (wallpaperInTile || glass != null) Color.Transparent else
                  tileColor.copy(alpha = brandTileAlpha(startStyle.tileAlpha, isBrandTile)),
              count = notificationCount,
              modifier = Modifier.fillMaxSize().graphicsLayer {
                  translationY = (1f - flipProgress.value) * size.height
              }
          )
      }
      }
        if (showEditControls) {
            val nextSize = tileSize.next()
            TileEditControls(
                label = app.label,
                resizeLabel = "Resize ${app.label} to ${nextSize.name.lowercase(Locale.getDefault())}",
                expanding = nextSize.columns * nextSize.rows > tileSize.columns * tileSize.rows,
                onUnpin = onUnpin,
                onResize = onResize,
                controlsOnStartSide = editControlsOnStartSide,
                insetEnd = editControlsInsetEnd
            )
        }
        if (notificationCount > 0 && !editing && showLiveContent) {
            // Live-content tiles: plain white count in the bottom-right corner: plain white count in the tile's bottom-right corner.
            AnimatedContent(
                targetState = notificationCount,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                },
                label = "notification-count",
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(end = if (tileSize == TileSize.Small) 4.dp else 8.dp,
                        bottom = if (tileSize == TileSize.Small) 1.dp else 4.dp)
                    .graphicsLayer { if (notificationFaceAvailable) alpha = 1f - flipProgress.value }
            ) { count ->
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    color = Color.White,
                    fontFamily = MetroRegular,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    fontSize = if (tileSize == TileSize.Small) 12.sp else 15.sp,
                    maxLines = 1
                )
            }
        }
    }
}
@Composable
private fun AnimatedTileText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    maxLines: Int
) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            (slideInVertically { it / 2 } + fadeIn()) togetherWith
                (slideOutVertically { -it / 2 } + fadeOut())
        },
        label = "live-tile-text"
    ) { animatedText ->
        Text(
            text = animatedText,
            color = Color.White,
            fontFamily = MetroRegular,
            fontWeight = FontWeight.Light,
            fontSize = fontSize,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun LauncherSettingsScreen(
    accentColor: Color,
    notificationAccessEnabled: Boolean,
    weather: WeatherTileState,
    weatherOptions: WeatherPreferences,
    onWeatherOptionsChange: (WeatherPreferences) -> Unit,
    onRefreshWeather: () -> Unit,
    startStyle: StartStyle,
    hasWallpaper: Boolean,
    wallpaperError: String?,
    onOpenWallpaperPicker: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onClearWallpaper: () -> Unit,
    onSelectAccent: (Color) -> Unit,
    onSelectStartStyle: (StartStyle) -> Unit,
    glassEnabled: Boolean,
    onGlassEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    // The classic Windows Phone accent palette.
    val accentOptions = listOf(
        "Lime" to Color(0xFFA4C400),
        "Green" to Color(0xFF60A917),
        "Emerald" to Color(0xFF008A00),
        "Teal" to Color(0xFF00ABA9),
        "Cyan" to Color(0xFF1BA1E2),
        "Cobalt" to Color(0xFF0050EF),
        "Indigo" to Color(0xFF6A00FF),
        "Violet" to Color(0xFFAA00FF),
        "Pink" to Color(0xFFF472D0),
        "Magenta" to Color(0xFFD80073),
        "Crimson" to Color(0xFFA20025),
        "Red" to Color(0xFFE51400),
        "Orange" to Color(0xFFFA6800),
        "Amber" to Color(0xFFF0A30A),
        "Yellow" to Color(0xFFE3C800),
        "Brown" to Color(0xFF825A2C),
        "Olive" to Color(0xFF6D8764),
        "Steel" to Color(0xFF647687),
        "Mauve" to Color(0xFF76608A),
        "Sienna" to Color(0xFFA0522D)
    )

    var navForward by remember { mutableStateOf(true) }
    fun open(path: String) { navForward = true; selectedCategory = path }
    fun goBack() {
        navForward = false
        selectedCategory = selectedCategory?.substringBeforeLast("/", "")?.ifEmpty { null }
    }
    BackHandler(enabled = selectedCategory != null) { goBack() }
    androidx.compose.runtime.DisposableEffect(Unit) {
        SettingsBack.handler = { if (selectedCategory != null) { goBack(); true } else false }
        onDispose { SettingsBack.handler = null }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    fun openAndroidSettings(action: String) {
        try {
            context.startActivity(Intent(action))
        } catch (_: android.content.ActivityNotFoundException) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    val pageStates = androidx.compose.runtime.saveable.rememberSaveableStateHolder()
    androidx.compose.runtime.CompositionLocalProvider(LocalSettingsAccent provides accentColor) {
    AnimatedContent(
        targetState = selectedCategory,
        modifier = modifier.fillMaxSize().background(SettingsBackground),
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        label = "settings-page"
    ) { pageCategory ->
        // W10M turnstile: pages swing on their left edge; the old page leaves first.
        val forward = navForward
        val angle by transition.animateFloat(transitionSpec = {
            if (targetState == EnterExitState.Visible) tween(300, delayMillis = 140, easing = MetroEnterEasing) else tween(150, easing = MetroExitEasing)
        }, label = "turnstile") { st -> when (st) {
            EnterExitState.PreEnter -> if (forward) -70f else 70f
            EnterExitState.Visible -> 0f
            EnterExitState.PostExit -> if (forward) 70f else -70f } }
        val fade by transition.animateFloat(transitionSpec = {
            if (targetState == EnterExitState.Visible) tween(120, delayMillis = 140) else tween(150) }, label = "turnstile-alpha") { if (it == EnterExitState.Visible) 1f else 0f }
        Box(Modifier.fillMaxSize().graphicsLayer {
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, .5f); rotationY = angle; alpha = fade; cameraDistance = 10f * density
        }) {
        pageStates.SaveableStateProvider(pageCategory ?: "settings-home") {
            if (pageCategory == null) {
                MetroSettingsHome(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onCategory = { open(it.title) },
                    accentColor = accentColor
                )
            } else {
                val segs = pageCategory.split("/")
                MetroSettingsPage(segs.last(), accentColor, onBack = { goBack() }, iconCategory = segs.first(),
                    parentTitle = if (segs.size > 1) segs.dropLast(1).last() else "Settings") {
            when (pageCategory) {
            "Extras" -> {
                SettingsAction("Weather", "Location, units, live tile background", accentColor) { open("Extras/Weather") }
                SettingsAction("Calendar live tile", "Allow calendar access in App permissions to show appointments", accentColor) {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)))
                }
            }
            "Extras/Weather" -> WeatherSettingsContent(weatherOptions, weather, accentColor, onWeatherOptionsChange, onRefreshWeather)
            "System" -> {
                SettingsAction("Display", "Brightness, screen timeout, text size", accentColor) {
                    openAndroidSettings(Settings.ACTION_DISPLAY_SETTINGS)
                }
                SettingsAction("Notifications & actions",
                    if (notificationAccessEnabled) "Live tile notifications are on" else "Enable notification counts and previews for pinned apps",
                    accentColor, onOpenNotificationSettings)
                SettingsAction("Spartan Bar", "Status bar look and items", accentColor) { open("System/Spartan Bar") }
                SettingsAction("Language", AppLanguage.options.firstOrNull { it.first == AppLanguage.current(context) }?.second ?: "Use system language", accentColor) { open("System/Language") }
                SettingsAction("Battery", "Battery use and power saving", accentColor) {
                    openAndroidSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                }
            }
            "System/Spartan Bar" -> SpartanBarSettings(accentColor)
            "System/Language" -> {
                Text("Choose the language Spartan Launcher uses. This doesn't change your phone's language.",
                    color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
                val currentTag = remember { AppLanguage.current(context) }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppLanguage.options.forEach { (tag, label) ->
                        Row(Modifier.fillMaxWidth().metroTilt {
                            (context as? android.app.Activity)?.let { AppLanguage.apply(it, tag) }
                        }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            MetroRadio(
                                selected = currentTag == tag, onClick = null,
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = accentColor, unselectedColor = Color.Gray))
                            Spacer(Modifier.width(8.dp))
                            Text(label, color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
                        }
                    }
                }
            }
            "Personalization" -> {
                SettingsAction("Start", "Background, People tile", accentColor) { open("Personalization/Start") }
                SettingsAction("Colors", accentOptions.firstOrNull { it.second == accentColor }?.first ?: "Custom color", accentColor) { open("Personalization/Colors") }
                SettingsAction("Sounds", "Ringtones and notification sounds", accentColor) { openAndroidSettings(Settings.ACTION_SOUND_SETTINGS) }
                SettingsAction("Lock screen", "Background, clock, Glance screen", accentColor) { open("Personalization/Lock screen") }
                SettingsAction("TrueSight", "Face and fingerprint unlock, greeting, PIN", accentColor) { open("Personalization/TrueSight") }
                SettingsAction("Themes", "Start layout style, glass tiles", accentColor) { open("Personalization/Themes") }
                SettingsAction("Icon pack", IconPacks.installed(context).firstOrNull { it.packageName == IconPacks.current }?.label ?: "Default", accentColor) { open("Personalization/Icon pack") }
            }
            "Personalization/Icon pack" -> {
                val packs = remember { IconPacks.installed(context) }
                Text("Choose the icons used on Start and in the app list. Messaging keeps its own icon.",
                    color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    (listOf(IconPackInfo("", "Default")) + packs).forEach { pack ->
                        Row(Modifier.fillMaxWidth().metroTilt {
                            if (pack.packageName != IconPacks.current) {
                                IconPacks.save(context, pack.packageName)
                                (context as? MainActivity)?.reloadIconsForPack()
                            }
                        }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            MetroRadio(
                                selected = IconPacks.current == pack.packageName, onClick = null,
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = accentColor, unselectedColor = Color.Gray))
                            Spacer(Modifier.width(8.dp))
                            Text(pack.label, color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
                        }
                    }
                    if (packs.isEmpty()) Text("No icon packs installed.", color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
                }
            }
            "Personalization/Themes" -> {
                Text(
                    text = "Start style",
                    color = Color.White,
                    fontFamily = MetroRegular,
                    fontSize = 20.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StartStyle.entries.forEach { style ->
                        Row(Modifier.fillMaxWidth().metroTilt { onSelectStartStyle(style) }
                            .padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            MetroRadio(
                                selected = startStyle == style, onClick = null,
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = accentColor, unselectedColor = Color.Gray))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (style == StartStyle.Windows10Mobile) "Modern" else "Classic",
                                    color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
                                Text(style.subtitle, color = SettingsSecondary,
                                    fontFamily = MetroRegular, fontSize = 14.sp)
                            }
                        }
                    }
                }
                if (startStyle == StartStyle.Windows10Mobile) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Glass tiles", color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
                            Text("Acrylic blur behind Start tiles", color = SettingsSecondary,
                                fontFamily = MetroRegular, fontSize = 14.sp)
                        }
                        MetroToggle(
                            checked = glassEnabled,
                            onCheckedChange = onGlassEnabledChange,
                            colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = accentColor)
                        )
                    }
                }
            }
            "Personalization/Start" -> {
                Text(
                    text = "Background",
                    color = Color.White,
                    fontFamily = MetroRegular,
                    fontSize = 20.sp
                )
                SettingsAction(
                    title = "Choose a photo",
                    subtitle = "Use an image behind your Start tiles",
                    accentColor = accentColor,
                    onClick = onOpenWallpaperPicker
                )
                BingWallpaperSettings()
                if (hasWallpaper) {
                    SettingsAction(
                        title = "Remove wallpaper",
                        subtitle = "Return to the plain background",
                        accentColor = accentColor,
                        onClick = onClearWallpaper
                    )
                }
                if (wallpaperError != null) {
                    Text(
                        text = wallpaperError,
                        color = Color(0xFFFFB4AB),
                        fontFamily = MetroRegular,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "People tile",
                    color = Color.White,
                    fontFamily = MetroRegular,
                    fontSize = 20.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PeopleTileStyle.entries.forEach { style ->
                        val selected = PeopleTileStyles.current == style
                        Row(Modifier.fillMaxWidth().metroTilt {
                            PeopleTileStyles.save(context, style)
                        }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            MetroRadio(
                                selected = selected, onClick = null,
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = accentColor, unselectedColor = Color.Gray))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (style == PeopleTileStyle.Windows10Mobile) "Modern" else "Classic",
                                    color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
                                Text(
                                    if (style == PeopleTileStyle.Windows10Mobile) "Rows of circles that roll"
                                    else "Photos mixed with accent blocks",
                                    color = SettingsSecondary,
                                    fontFamily = MetroRegular, fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            "Personalization/Colors" -> {
                Text(
                    text = "Accent color",
                    color = Color.White,
                    fontFamily = MetroRegular,
                    fontSize = 20.sp
                )
                Text(accentOptions.firstOrNull { it.second == accentColor }?.first ?: "Custom color",
                    color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 16.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    accentOptions.chunked(4).forEach { colors ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            colors.forEach { (name, color) ->
                                val isSelected = accentColor == color
                                Box(Modifier.weight(1f).height(48.dp).background(color)
                                    .then(if (isSelected) Modifier.border(2.dp, Color.White) else Modifier)
                                    .semantics {
                                        contentDescription = "$name accent color"
                                        this[androidx.compose.ui.semantics.SemanticsProperties.Selected] = isSelected
                                    }
                                    .metroTilt { onSelectAccent(color) }) {
                                    if (isSelected) Canvas(Modifier.align(Alignment.TopEnd).size(24.dp).padding(5.dp)) {
                                        val s = size.minDimension
                                        drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, s * .5f),
                                            androidx.compose.ui.geometry.Offset(s * .35f, s * .85f), 2.dp.toPx())
                                        drawLine(Color.White, androidx.compose.ui.geometry.Offset(s * .35f, s * .85f),
                                            androidx.compose.ui.geometry.Offset(s, s * .15f), 2.dp.toPx())
                                    }
                                }
                            }
                            repeat(4 - colors.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            "Personalization/Lock screen" -> {
                LockScreenSettings()
                SpartanLockSettings(onOpenTrueSight = { open("Personalization/TrueSight") })
            }
            "Personalization/TrueSight" -> TrueSightSettings()
            else -> {
                val actions = when (pageCategory) {
                    "Devices" -> listOf(
                        Triple("Bluetooth", "Connect and manage devices", Settings.ACTION_BLUETOOTH_SETTINGS),
                        Triple("Camera permissions", "Choose which apps can use your camera", Settings.ACTION_APPLICATION_SETTINGS))
                    "Network & wireless" -> listOf(
                        Triple("Wi-Fi", "Available networks and connections", Settings.ACTION_WIFI_SETTINGS),
                        Triple("Airplane mode & cellular", "Wireless and mobile network settings", Settings.ACTION_WIRELESS_SETTINGS))
                    "Accounts" -> listOf(Triple("Your accounts", "Manage accounts and sync", Settings.ACTION_SYNC_SETTINGS))
                    "Time & language" -> listOf(
                        Triple("Date & time", "Time zone and automatic time", Settings.ACTION_DATE_SETTINGS),
                        Triple("Language", "Region and display language", Settings.ACTION_LOCALE_SETTINGS),
                        Triple("Keyboard", "Input methods and typing", Settings.ACTION_INPUT_METHOD_SETTINGS))
                    "Ease of Access" -> listOf(Triple("Accessibility", "Text size, screen reader, and interaction", Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    "Privacy" -> listOf(
                        Triple("Location", "Location access and services", Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        Triple("App permissions", "Manage access to your personal information", Settings.ACTION_APPLICATION_SETTINGS))
                    else -> listOf(
                        Triple("Security", "Device security and protection", Settings.ACTION_SECURITY_SETTINGS),
                        Triple("About your phone", "Device information and software updates", Settings.ACTION_DEVICE_INFO_SETTINGS))
                }
                actions.forEach { (title, subtitle, action) ->
                    SettingsAction(title, subtitle, accentColor) { openAndroidSettings(action) }
                }
            }
            }
                }
            }
        }
        }
    }
}
}

@Composable
private fun SettingsAction(
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .metroTilt(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(title, color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
        Text(
            subtitle,
            color = SettingsSecondary,
            fontFamily = MetroRegular,
            fontSize = 14.sp
        )
    }
}

/**
 * Our contact tile, based on the Windows Phone 8.1 original: the front is the
 * photo full-bleed (the wide size adds the name over a bottom gradient), and it
 * flips WP-style to a back face with the person's name and contact details.
 * Better than both originals: it only flips when there is something to show.
 */
private const val ATLAS_PACKAGE = "com.example.spartanatlas"

/**
 * Web page pinned from Spartan Atlas, W10M Edge style: accent tile, favicon centered at ~40%
 * of the tile's short side (scaled to fit, never stretched), page title bottom-left like
 * other tiles. Small tiles show the icon only.
 */
@Composable
private fun AtlasWebTileContent(app: LauncherApp, accentColor: Color, tileSize: TileSize, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier.background(accentColor)) {
        val iconSide = minOf(maxWidth, maxHeight) * 0.4f
        Image(
            bitmap = app.icon,
            contentDescription = app.label,
            contentScale = ContentScale.Fit,
            filterQuality = androidx.compose.ui.graphics.FilterQuality.High,
            modifier = Modifier.align(Alignment.Center).size(iconSide)
        )
        if (tileSize != TileSize.Small) {
            Text(
                text = app.label,
                color = Color.White,
                fontFamily = MetroRegular,
                fontSize = when (tileSize) {
                    TileSize.Medium -> 13.sp
                    TileSize.Wide -> 14.sp
                    TileSize.Large -> 16.sp
                    TileSize.Small -> 12.sp
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
            )
        }
    }
}

@Composable
private fun PinnedShortcutTileContent(
    app: LauncherApp,
    accentColor: Color,
    tileSize: TileSize,
    animationsActive: Boolean,
    phone: String,
    email: String,
    modifier: Modifier = Modifier
) {
    if (pinnedShortcutSource(app.pinKey())?.first == ATLAS_PACKAGE) {
        AtlasWebTileContent(app, accentColor, tileSize, modifier)
        return
    }
    val hasBack = tileSize != TileSize.Small && (phone.isNotBlank() || email.isNotBlank())
    var back by remember(app.pinKey(), hasBack) { mutableStateOf(false) }
    val rotation = remember(app.pinKey()) { Animatable(0f) }
    LaunchedEffect(app.pinKey(), animationsActive, hasBack) {
        if (!animationsActive || !hasBack) return@LaunchedEffect
        delay(kotlin.random.Random.nextLong(4_500L, 7_000L))
        while (true) {
            // WP live tiles flip over their horizontal axis
            rotation.animateTo(90f, tween(420, easing = FastOutSlowInEasing))
            back = true
            rotation.snapTo(-90f)
            rotation.animateTo(0f, tween(420, easing = FastOutSlowInEasing))
            delay(4_500L)
            rotation.animateTo(90f, tween(420, easing = FastOutSlowInEasing))
            back = false
            rotation.snapTo(-90f)
            rotation.animateTo(0f, tween(420, easing = FastOutSlowInEasing))
            delay(kotlin.random.Random.nextLong(6_000L, 9_000L))
        }
    }
    Box(
        modifier.graphicsLayer {
            rotationX = rotation.value
            cameraDistance = 18f * density
        }
    ) {
        if (!back) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // The contact's name sits over a bottom gradient at every size,
            // like a pinned contact on Windows 10 Mobile.
            Box(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                        )
                    )
                    .padding(
                        start = if (tileSize == TileSize.Small) 6.dp else 9.dp,
                        end = if (tileSize == TileSize.Small) 6.dp else 9.dp,
                        top = if (tileSize == TileSize.Small) 12.dp else 18.dp,
                        bottom = if (tileSize == TileSize.Small) 5.dp else 7.dp
                    )
            ) {
                // Shrink to fit so a full name survives even on a small tile.
                var labelFontSize by remember(app.label, tileSize) {
                    mutableStateOf(
                        when (tileSize) {
                            TileSize.Small -> 10.sp
                            TileSize.Medium -> 13.sp
                            else -> 15.sp
                        }
                    )
                }
                Text(
                    text = app.label,
                    color = Color.White,
                    fontFamily = MetroRegular,
                    fontSize = labelFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { result ->
                        if (result.hasVisualOverflow && labelFontSize.value > 7f) {
                            labelFontSize *= 0.92f
                        }
                    }
                )
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(accentColor)
                    .padding(horizontal = 11.dp, vertical = 10.dp)
            ) {
                Text(
                    text = app.label,
                    color = Color.White,
                    fontFamily = MetroRegular,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    fontSize = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (phone.isNotBlank()) {
                    Text(
                        text = "Mobile",
                        color = Color.White.copy(alpha = 0.7f),
                        fontFamily = MetroRegular,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        text = phone,
                        color = Color.White,
                        fontFamily = MetroRegular,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (email.isNotBlank()) {
                    Text(
                        text = "Email",
                        color = Color.White.copy(alpha = 0.7f),
                        fontFamily = MetroRegular,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = email,
                        color = Color.White,
                        fontFamily = MetroRegular,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PeopleTileContent(
    animationsActive: Boolean,
    state: PeopleTileState,
    accentColor: Color,
    onWallpaper: Boolean = false,
    insideBorder: Boolean = false,
    modifier: Modifier = Modifier
) {
    val photoContacts = remember(state.contacts) {
        state.contacts.filter { it.photo != null }
    }
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val columns = (maxWidth / (maxHeight / 3)).toInt().coerceAtLeast(3)
        val cellSize = maxWidth / columns
        val rows = ceil(maxHeight / cellSize).toInt().coerceAtLeast(3)
        val cellsPerPage = columns * rows
        // Stable ordering survives freshly decoded contact photos on resume.
        val shuffledContacts = remember(photoContacts) { photoContacts.sortedBy { it.id.hashCode() } }
        if (photoContacts.isNotEmpty() && PeopleTileStyles.current == PeopleTileStyle.Windows10Mobile) {
            Windows10PeopleCircles(animationsActive, photoContacts, maxWidth, maxHeight, insideBorder)
        } else if (photoContacts.isNotEmpty()) {
            ClassicPeopleMosaic(animationsActive, photoContacts, accentColor, maxWidth, maxHeight)
        } else if (state.loading || state.error != null) {
            Text(
                text = if (state.loading) "Loading people..." else state.error.orEmpty(),
                color = Color.White,
                fontFamily = MetroRegular,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 12.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 5.dp, bottom = 4.dp)
        ) {
            Text(
                text = "People",
                color = Color.White,
                fontFamily = MetroRegular,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun Windows10PeopleCircles(
    animationsActive: Boolean,
    photos: List<PeopleTileContact>,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    insideBorder: Boolean
) {
    val (columns, rows) = peopleCircleGrid(width.value, height.value)
    if (columns == 1 && rows == 1) { SmallPeopleOrbit(animationsActive, photos, width, height); return }
    val photoIds = photos.map { it.id }
    val square = columns == 3 && rows == 3
    var lines by remember(photoIds, columns, rows) {
        mutableStateOf(List(rows) { row ->
            List(columns) { column ->
                val n = row * columns + column
                if (n % 3 == 2) -10 - (n % 4) else n % photos.size
            }
        })
    }
    var rowShift by remember(rows) { mutableStateOf(List(rows) { 0f }) }
    var colShift by remember(columns) { mutableStateOf(List(columns) { 0f }) }
    // -3 means nothing is waiting off that edge.
    var rowExtraFace by remember(rows) { mutableStateOf(List(rows) { -3 }) }
    var rowExtraSlot by remember(rows) { mutableStateOf(List(rows) { 0 }) }
    var colExtraFace by remember(columns) { mutableStateOf(List(columns) { -3 }) }
    var colExtraSlot by remember(columns) { mutableStateOf(List(columns) { 0 }) }
    var diagDx by remember { mutableIntStateOf(1) }
    var diagDy by remember { mutableIntStateOf(1) }
    var diagShift by remember { mutableStateOf(0f) }
    var diagIncoming by remember { mutableStateOf(emptyList<Triple<Int, Int, Int>>()) }
    LaunchedEffect(photoIds, animationsActive, columns, rows) {
        rowShift = List(rows) { 0f }
        colShift = List(columns) { 0f }
        rowExtraFace = List(rows) { -3 }
        colExtraFace = List(columns) { -3 }
        diagShift = 0f
        diagIncoming = emptyList()
        if (photos.isEmpty() || !animationsActive) return@LaunchedEffect
        var cursor = columns * rows
        fun fresh(): Int {
            val face = if (cursor % 3 == 0) -10 - (cursor / 3 % 4) else cursor % photos.size
            cursor++
            return face
        }
        delay(900L)
        while (true) {
            val spins = if (kotlin.random.Random.nextFloat() < 0.5f) 2 else 1
            when (kotlin.random.Random.nextInt(2)) {
            0 -> {
                val row = kotlin.random.Random.nextInt(rows)
                val toRight = kotlin.random.Random.nextBoolean()
                repeat(spins) {
                    val face = fresh()
                    val travel = Animatable(0f)
                    rowExtraFace = rowExtraFace.toMutableList().also { it[row] = face }
                    rowExtraSlot = rowExtraSlot.toMutableList().also { it[row] = if (toRight) -1 else columns }
                    rowShift = rowShift.toMutableList().also { it[row] = 0f }
                    travel.animateTo(if (toRight) 1f else -1f, tween(900, easing = androidx.compose.animation.core.CubicBezierEasing(0.45f, 0f, 0.15f, 1f))) {
                        rowShift = rowShift.toMutableList().also { it[row] = value }
                    }
                    lines = lines.toMutableList().also { copy ->
                        copy[row] = if (toRight) listOf(face) + copy[row].dropLast(1) else copy[row].drop(1) + face
                    }
                    rowShift = rowShift.toMutableList().also { it[row] = 0f }
                    rowExtraFace = rowExtraFace.toMutableList().also { it[row] = -3 }
                }
            }
            1 -> {
                val col = kotlin.random.Random.nextInt(columns)
                val down = kotlin.random.Random.nextBoolean()
                repeat(spins) {
                    val face = fresh()
                    val travel = Animatable(0f)
                    colExtraFace = colExtraFace.toMutableList().also { it[col] = face }
                    colExtraSlot = colExtraSlot.toMutableList().also { it[col] = if (down) -1 else rows }
                    colShift = colShift.toMutableList().also { it[col] = 0f }
                    travel.animateTo(if (down) 1f else -1f, tween(900, easing = androidx.compose.animation.core.CubicBezierEasing(0.45f, 0f, 0.15f, 1f))) {
                        colShift = colShift.toMutableList().also { it[col] = value }
                    }
                    val next = List(rows) { r ->
                        when {
                            down && r == 0 -> face
                            down -> lines[r - 1][col]
                            r == rows - 1 -> face
                            else -> lines[r + 1][col]
                        }
                    }
                    lines = lines.mapIndexed { r, rowFaces ->
                        rowFaces.toMutableList().also { it[col] = next[r] }
                    }
                    colShift = colShift.toMutableList().also { it[col] = 0f }
                    colExtraFace = colExtraFace.toMutableList().also { it[col] = -3 }
                }
            }
            else -> {
                val dx = if (kotlin.random.Random.nextBoolean()) 1 else -1
                val dy = if (kotlin.random.Random.nextBoolean()) 1 else -1
                diagDx = dx
                diagDy = dy
                repeat(spins) {
                    val incoming = buildList {
                        for (r in 0 until rows) for (c in 0 until columns) {
                            val srcRow = r - dy
                            val srcCol = c - dx
                            if (srcRow !in 0 until rows || srcCol !in 0 until columns) add(Triple(r, c, fresh()))
                        }
                    }
                    val travel = Animatable(0f)
                    diagIncoming = incoming
                    diagShift = 0f
                    travel.animateTo(1f, tween(900, easing = androidx.compose.animation.core.CubicBezierEasing(0.45f, 0f, 0.15f, 1f))) {
                        diagShift = value
                    }
                    val arrived = incoming.associate { (r, c, face) -> (r to c) to face }
                    lines = List(rows) { r ->
                        List(columns) { c ->
                            val srcRow = r - dy
                            val srcCol = c - dx
                            if (srcRow in 0 until rows && srcCol in 0 until columns) lines[srcRow][srcCol]
                            else arrived.getValue(r to c)
                        }
                    }
                    diagShift = 0f
                    diagIncoming = emptyList()
                }
            }
            }
            delay(kotlin.random.Random.nextLong(1_400L, 3_200L))
        }
    }
    // The glass stroke sits about 2.dp in. Keep clipped circles off that line.
    val edge = if (insideBorder) 3.dp else 0.dp
    val frameWidth = width - edge * 2
    val frameHeight = height - edge * 2
    val pitch = if (square) minOf(frameWidth, frameHeight) / 2.35f
        else minOf((frameWidth - 4.dp) / columns, (frameHeight - 4.dp) / rows)
    val circle = pitch * 0.92f
    val originX = (frameWidth - (pitch * (columns - 1) + circle)) / 2
    val originY = (frameHeight - (pitch * (rows - 1) + circle)) / 2
    Box(Modifier.padding(edge).fillMaxSize().clipToBounds()) {
        repeat(rows) { row ->
            val moved = rowShift[row]
            repeat(columns) { col ->
                val dropped = colShift[col]
                val slide = diagShift
                PeopleCircleFace(
                    photos, lines[row][col],
                    Modifier
                        .offset(
                            x = originX + pitch * (col + moved + diagDx * slide),
                            y = originY + pitch * (row + dropped + diagDy * slide)
                        )
                        .size(circle)
                        .graphicsLayer { rotationZ = (moved - dropped + diagDx * slide) * 360f }
                )
            }
            val extra = rowExtraFace[row]
            if (extra != -3) {
                PeopleCircleFace(
                    photos, extra,
                    Modifier
                        .offset(
                            x = originX + pitch * (rowExtraSlot[row] + moved),
                            y = originY + pitch * row
                        )
                        .size(circle)
                        .graphicsLayer { rotationZ = moved * 360f }
                )
            }
        }
        repeat(columns) { col ->
            val extra = colExtraFace[col]
            if (extra != -3) {
                val dropped = colShift[col]
                PeopleCircleFace(
                    photos, extra,
                    Modifier
                        .offset(
                            x = originX + pitch * col,
                            y = originY + pitch * (colExtraSlot[col] + dropped)
                        )
                        .size(circle)
                        .graphicsLayer { rotationZ = -dropped * 360f }
                )
            }
        }
        diagIncoming.forEach { (entryRow, entryCol, face) ->
            PeopleCircleFace(
                photos, face,
                Modifier
                    .offset(
                        x = originX + pitch * (entryCol - diagDx + diagDx * diagShift),
                        y = originY + pitch * (entryRow - diagDy + diagDy * diagShift)
                    )
                    .size(circle)
                    .graphicsLayer { rotationZ = diagDx * diagShift * 360f }
            )
        }
    }
}

@Composable
private fun PeopleCircleFace(photos: List<PeopleTileContact>, index: Int, modifier: Modifier) {
    val dot = if (index <= -10) floatArrayOf(0.10f, 0.18f, 0.26f, 0.34f)[(-10 - index) % 4] else 0f
    Box(modifier.clip(CircleShape).then(if (dot > 0f) Modifier.background(Color.White.copy(alpha = dot)) else Modifier)) {
        photos.getOrNull(index)?.photo?.let { photo ->
            Image(photo, photos[index].name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
private fun ContactMosaicCell(
    animationsActive: Boolean,
    contacts: List<PeopleTileContact>,
    accentColor: Color,
    onWallpaper: Boolean,
    shade: Float,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val contactIds = contacts.map { it.id }
    var shownContactId by remember(contactIds) {
        mutableStateOf(if (kotlin.random.Random.nextBoolean()) null else contactIds.randomOrNull())
    }
    val currentContacts by rememberUpdatedState(contacts)
    var accentShade by remember(accentColor) {
        mutableStateOf(randomPeopleAccentShade(accentColor))
    }
    var wallpaperShade by remember(shade) { mutableStateOf(shade) }
    val shownShade by animateFloatAsState(wallpaperShade, tween(700), label = "people-shade")
    val rotation = remember { Animatable(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    LaunchedEffect(contactIds, animationsActive, accentColor) {
        rotation.snapTo(0f)
        if (contacts.isNotEmpty() && animationsActive) {
            delay(kotlin.random.Random.nextLong(2_000L, 8_000L))
            while (true) {
                rotation.animateTo(
                    90f,
                    tween(kotlin.random.Random.nextInt(500, 750), easing = FastOutSlowInEasing)
                )
                if (kotlin.random.Random.nextBoolean()) {
                    if (onWallpaper) wallpaperShade = peopleWallpaperShade.random()
                    else accentShade = randomPeopleAccentShade(accentColor)
                    shownContactId = null
                } else {
                    val candidates = currentContacts.filterNot { it.id == shownContactId }
                    shownContactId = (candidates.randomOrNull() ?: currentContacts.firstOrNull())?.id
                }
                rotation.snapTo(-90f)
                rotation.animateTo(
                    0f,
                    tween(kotlin.random.Random.nextInt(500, 750), easing = FastOutSlowInEasing)
                )
                delay(kotlin.random.Random.nextLong(8_000L, 16_000L))
            }
        }
    }
    val photoContact = contacts.firstOrNull { it.id == shownContactId }
    val photo = photoContact?.photo
    val wallpaperBlock = onWallpaper && photo == null
    Box(
        modifier = modifier
            .size(size)
            .clipToBounds()
            .then(
                if (wallpaperBlock && rotation.value == 0f) Modifier else Modifier.graphicsLayer {
                    cameraDistance = 8f * density.density
                    rotationX = rotation.value
                }
            )
            .background(if (wallpaperBlock) Color.Black.copy(alpha = shownShade) else accentShade),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = photoContact.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private val peopleWallpaperShade = floatArrayOf(0.05f, 0.2f, 0.38f, 0.55f, 0.72f)

private fun randomPeopleAccentShade(accentColor: Color): Color =
    androidx.compose.ui.graphics.lerp(
        Color.Black,
        accentColor,
        0.4f + kotlin.random.Random.nextFloat() * 0.6f
    ).copy(alpha = accentColor.alpha)

@Composable
internal fun PhotosTileContent(
    animationsActive: Boolean,
    state: PhotosTileState,
    tileLabel: String,
    modifier: Modifier = Modifier
) {
    var imageIndex by remember(state.photoIds) { mutableIntStateOf(0) }
    LaunchedEffect(state.photoIds, state.photos.size, animationsActive) {
        if (state.photos.size > 1 && animationsActive) {
            while (true) {
                delay(8_000L)
                imageIndex = (imageIndex + 1) % state.photos.size
            }
        }
    }

    if (state.photos.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.12f))
                .padding(14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = when {
                    state.loading -> "Loading photos..."
                    state.error != null -> state.error
                    else -> "No recent photos"
                },
                color = Color.White,
                fontFamily = MetroRegular,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            Box(Modifier.fillMaxSize().clipToBounds()) {
                AnimatedContent(
                    // Keep the outgoing image itself, never an index into a changing library.
                    targetState = state.photos[imageIndex.coerceIn(state.photos.indices)] to (imageIndex % 2),
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        fadeIn(tween(900)) togetherWith fadeOut(tween(900))
                    },
                    label = "photos-image-crossfade"
                ) { (image, phase) ->
                    // Each crossfade face owns its pan, so outgoing photos never jump.
                    val glideProgress = remember(image) { Animatable(0f) }
                    LaunchedEffect(image, animationsActive) {
                        if (animationsActive) {
                            glideProgress.animateTo(
                                1f,
                                tween(
                                    (8_000 * (1f - glideProgress.value)).roundToInt().coerceAtLeast(1),
                                    easing = androidx.compose.animation.core.LinearEasing
                                )
                            )
                        }
                    }
                    Image(
                        bitmap = image,
                        contentDescription = tileLabel,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val zoom = 1.12f
                                scaleX = zoom
                                scaleY = zoom
                                val direction = if (phase == 0) 1f else -1f
                                // Stay inside the zoomed image's margin, including wide tiles.
                                translationX = direction * size.width * (zoom - 1f) *
                                    0.7f * (glideProgress.value - 0.5f)
                                translationY = -direction * size.height * (zoom - 1f) *
                                    0.7f * (glideProgress.value - 0.5f)
                            }
                    )
                }
                Box(
                    Modifier.fillMaxSize().background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                        )
                    )
                )
                Text(
                    text = tileLabel,
                    color = Color.White,
                    fontFamily = MetroRegular,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MetroTileIcon(kind: TileKind, modifier: Modifier = Modifier) {
    if (kind == TileKind.Messages) {
        MessagingTileIcon(0, modifier)
        return
    }
    if (kind == TileKind.Music) {
        MetroMusicGlyph(modifier, classic = true)
        return
    }
    val imageVector = when (kind) {
        TileKind.Apps -> FluentIcons.Filled.AppsList
        TileKind.Phone -> FluentIcons.Filled.Call
        TileKind.Messages -> FluentIcons.Filled.Chat
        TileKind.Browser -> FluentIcons.Filled.Globe
        TileKind.Photos -> FluentIcons.Filled.Image
        TileKind.Camera -> FluentIcons.Filled.Camera
        TileKind.Mail -> FluentIcons.Filled.Mail
        TileKind.Calendar -> FluentIcons.Filled.CalendarLtr
        TileKind.Maps -> FluentIcons.Filled.Map
        TileKind.Settings -> FluentIcons.Regular.Settings
        TileKind.Store -> FluentIcons.Filled.BuildingShop
        TileKind.People -> FluentIcons.Filled.People
        TileKind.Files -> FluentIcons.Filled.Folder
        TileKind.Music -> FluentIcons.Filled.WeatherSunny
        TileKind.Clock -> FluentIcons.Filled.Clock
        TileKind.Weather -> FluentIcons.Filled.WeatherSunny
    }
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = Color.White,
        modifier = modifier
    )
}

/** Windows 10 Mobile-style flyout for an app list row: dark sheet, square corners. */
@Composable
private fun AppListFlyoutMenu(
    expanded: Boolean,
    pinned: Boolean,
    canUninstall: Boolean,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onUninstall: () -> Unit
) {
    val itemPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 11.dp)
    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(248.dp),
        shape = androidx.compose.ui.graphics.RectangleShape,
        containerColor = Color(0xFF1F1F1F),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF454545)),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        androidx.compose.material3.DropdownMenuItem(
            text = {
                Text(
                    if (pinned) "Unpin from Start" else "Pin to Start",
                    color = Color.White, fontFamily = MetroRegular, fontSize = 15.sp
                )
            },
            onClick = onTogglePin,
            contentPadding = itemPadding
        )
        if (canUninstall) {
            androidx.compose.material3.DropdownMenuItem(
                text = {
                    Text("Uninstall", color = Color.White, fontFamily = MetroRegular, fontSize = 15.sp)
                },
                onClick = onUninstall,
                contentPadding = itemPadding
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun AllAppsScreen(
    backgroundShade: State<Float>,
    onJumpListChange: (Boolean) -> Unit,
    homeEntryCount: Int,
    isExiting: Boolean,
    apps: List<LauncherApp>,
    appsLoading: Boolean,
    pinnedAppIds: Set<String>,
    accentColor: Color,
    startStyle: StartStyle,
    query: String,
    onQueryChange: (String) -> Unit,
    onTogglePinnedApp: (LauncherApp) -> Unit,
    onLaunch: LaunchHandler,
    onUninstallApp: (LauncherApp) -> Unit,
    wallpaperImage: ImageBitmap? = null,
    wallpaperRootSize: IntSize = IntSize.Zero,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val appGroups = remember(apps, query) {
        val collator = appListCollator()
        val grouped = apps.distinctBy { it.pinKey() }
            .filter { matchesAppSearch(it.label, query) }
            .sortedWith { left, right ->
                collator.compare(left.label.trim(), right.label.trim()).takeIf { it != 0 }
                    ?: left.pinKey().compareTo(right.pinKey())
            }
            .groupBy { appListSection(it.label) }
        orderedAppSections(grouped.keys).associateWith { grouped.getValue(it) }
    }
    val groupStartIndices = remember(appGroups) {
        appListSectionStarts(appGroups.map { (section, apps) -> section to apps.size })
    }
    val currentSectionStarts by rememberUpdatedState(groupStartIndices)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showJumpList by remember { mutableStateOf(false) }
    var expandedMenuKey by remember { mutableStateOf<String?>(null) }
    var jumpJob by remember { mutableStateOf<Job?>(null) }
    val wp81List = startStyle == StartStyle.WindowsPhone81
    // WP8.1: the list sits right of a gutter holding the round search button.
    val listGutter = if (wp81List) 62.dp else 10.dp
    var wp81SearchOpen by remember { mutableStateOf(false) }
    val searchFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(wp81SearchOpen) { if (wp81SearchOpen) { delay(30); runCatching { searchFocus.requestFocus() } } }
    // Returning home / leaving All apps: close search and drop the keyboard so it never pops up unasked.
    LaunchedEffect(homeEntryCount, isExiting) {
        wp81SearchOpen = false
        focusManager.clearFocus(force = true)
        if (query.isNotEmpty()) onQueryChange("")
    }
    val currentJumpCallback by rememberUpdatedState(onJumpListChange)
    LaunchedEffect(showJumpList) { currentJumpCallback(showJumpList) }
    LaunchedEffect(query) {
        jumpJob?.cancel()
        showJumpList = false
        expandedMenuKey = null
        listState.scrollToItem(0)
    }
    LaunchedEffect(homeEntryCount, isExiting) {
        showJumpList = false
        expandedMenuKey = null
    }
    BackHandler(enabled = query.isNotEmpty() && !showJumpList) {
        focusManager.clearFocus()
        onQueryChange("")
    }
    BackHandler(enabled = showJumpList) { showJumpList = false }
    BackHandler(enabled = wp81List && wp81SearchOpen && query.isEmpty() && !showJumpList) {
        focusManager.clearFocus(); wp81SearchOpen = false
    }
    val searchHintColor = MetroTheme.colors.subtle
    val motion = rememberMetroMotion(
        isExiting,
        ready = apps.isNotEmpty() && !appsLoading,
        entranceDelayMillis = 90
    )
    val currentExiting by rememberUpdatedState(isExiting)
    // The launch dive tips around the tapped row, same as tiles on Start.
    var listPivot by remember { mutableStateOf(Offset(0.5f, 0.4f)) }
    var pageWindowPos by remember { mutableStateOf(Offset.Zero) }
    var pageSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    Box(
        modifier = modifier.fillMaxSize()
            .onGloballyPositioned {
                pageWindowPos = it.positionInWindow()
                pageSize = it.size
            }
            .metroSwivel(listPivot, exiting = { currentExiting }) { motion.value }
    ) {
        // SpartanBar is an overlay: push the list below whatever it currently
        // shows (bar alone, or bar + call/live strip), following it smoothly.
        val barDensity = androidx.compose.ui.platform.LocalDensity.current
        // Exactly SpartanBar's current bottom (bar, or bar + strip); otherwise
        // the system status bar / cutout.
        val sysTopPx = maxOf(
            androidx.compose.foundation.layout.WindowInsets.safeDrawing.getTop(barDensity),
            androidx.compose.foundation.layout.WindowInsets.statusBarsIgnoringVisibility.getTop(barDensity))
        val barExtra by androidx.compose.animation.core.animateDpAsState(
            with(barDensity) {
                (if (SpartanBarAccess.enabled && SpartanBarAccess.insetPx > 0) SpartanBarAccess.insetPx
                 else sysTopPx).toDp()
            },
            tween(220, easing = FastOutSlowInEasing), label = "barInset"
        )
        Column(Modifier.fillMaxSize().imePadding().padding(top = barExtra)) {
        if (!wp81List || wp81SearchOpen || query.isNotEmpty()) BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = MetroTheme.colors.fg,
                fontFamily = MetroRegular,
                fontSize = 17.sp
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(MetroTheme.colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(searchFocus)
                .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 4.dp)
                .background(Color.Black.copy(alpha = 0.45f))
                .border(1.dp, Color(0xFFA0A0A0)),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                "Search",
                                color = searchHintColor,
                                fontFamily = MetroRegular,
                                fontSize = 17.sp
                            )
                        }
                        innerTextField()
                    }
                    // W10M: magnifier sits at the right end of the box.
                    if (query.isEmpty()) {
                    Canvas(Modifier.size(18.dp)) {
                        val stroke = 1.8.dp.toPx()
                        drawCircle(
                            color = searchHintColor,
                            radius = 6.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(
                                x = size.width * 0.43f,
                                y = size.height * 0.43f
                            ),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
                        )
                        drawLine(
                            color = searchHintColor,
                            start = androidx.compose.ui.geometry.Offset(
                                x = size.width * 0.62f,
                                y = size.height * 0.62f
                            ),
                            end = androidx.compose.ui.geometry.Offset(
                                x = size.width * 0.9f,
                                y = size.height * 0.9f
                            ),
                            strokeWidth = stroke
                        )
                    }
                    }
                    if (query.isNotEmpty()) {
                        Box(
                            Modifier.size(40.dp)
                                .semantics { contentDescription = "Clear search" }
                                .clickable { onQueryChange("") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("×", color = Color.White, fontSize = 28.sp)
                        }
                    }
                }
            }
        )
            if (appsLoading && apps.isEmpty()) {
                MetroAppsLoading(
                    accent = accentColor,
                    phoneDots = startStyle == StartStyle.WindowsPhone81,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            } else {
                Box(Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        userScrollEnabled = !isExiting && !showJumpList,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp +
                            androidx.compose.foundation.layout.WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        appGroups.forEach { (section, sectionApps) ->
                            stickyHeader(key = "group-$section") {
                                Box(Modifier.fillMaxWidth()
                                    .appListHeaderBackground(
                                        wallpaper = wallpaperImage.takeIf { startStyle == StartStyle.Windows10Mobile },
                                        rootSize = wallpaperRootSize,
                                        backgroundColor = MetroTheme.colors.bg,
                                        shade = backgroundShade
                                    )
                                    .padding(start = listGutter,
                                        top = if (startStyle == StartStyle.Windows10Mobile) 2.dp else 8.dp,
                                        bottom = if (startStyle == StartStyle.Windows10Mobile) 5.dp else 6.dp)) {
                                    val w10Header = startStyle == StartStyle.Windows10Mobile
                                    Box(
                                        // WP8.1: a solid accent square the size of the app icons, aligned with them.
                                        // WP8.1: an accent-outlined square the size of the app icons.
                                        (if (w10Header) Modifier.height(40.dp).width(48.dp) else Modifier.size(46.dp))
                                            .then(if (w10Header) Modifier else Modifier.border(2.dp, accentColor))
                                            .semantics { contentDescription = "Open alphabet picker at $section" }
                                            .clickable(enabled = !isExiting) {
                                                focusManager.clearFocus()
                                                expandedMenuKey = null
                                                showJumpList = true
                                            },
                                        // W10M: plain white capital, left-aligned, no box.
                                        contentAlignment = if (w10Header) Alignment.CenterStart else Alignment.BottomStart
                                    ) {
                                        Text(
                                            if (w10Header) section.uppercase(Locale.ROOT) else section.lowercase(Locale.ROOT),
                                            color = if (w10Header) Color.White else accentColor,
                                            fontFamily = MetroRegular,
                                            fontSize = if (w10Header) 30.sp else 26.sp,
                                            lineHeight = if (w10Header) 30.sp else 26.sp,
                                            maxLines = 1,
                                            modifier = if (w10Header) Modifier else Modifier.padding(start = 5.dp, bottom = 1.dp)
                                        )
                                    }
                                }
                            }
                            items(sectionApps, key = { it.pinKey() }) { app ->
                                var rowBounds by remember { mutableStateOf<android.graphics.Rect?>(null) }
                                Box(
                                    Modifier.fillMaxWidth()
                                        .onGloballyPositioned { coords ->
                                            val bounds = coords.boundsInWindow()
                                            rowBounds = android.graphics.Rect(
                                                bounds.left.roundToInt(), bounds.top.roundToInt(),
                                                bounds.right.roundToInt(), bounds.bottom.roundToInt()
                                            )
                                        }
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth()
                                            .combinedClickable(
                                                enabled = !isExiting,
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    expandedMenuKey = null
                                                    rowBounds?.let { b ->
                                                        if (pageSize.width > 0 && pageSize.height > 0) {
                                                            listPivot = Offset(
                                                                ((b.exactCenterX() - pageWindowPos.x) / pageSize.width).coerceIn(0f, 1f),
                                                                ((b.exactCenterY() - pageWindowPos.y) / pageSize.height).coerceIn(0f, 1f)
                                                            )
                                                        }
                                                    }
                                                    onLaunch(app, rowBounds)
                                                },
                                                onLongClick = {
                                                    focusManager.clearFocus()
                                                    expandedMenuKey = app.pinKey()
                                                }
                                            )
                                            .padding(start = listGutter, end = 10.dp, top = if (wp81List) 4.dp else 2.dp, bottom = if (wp81List) 4.dp else 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val kind = tileKindForApp(app)
                                        val brand = appBrandFor(app)
                                        Box(
                                            Modifier.size(if (wp81List) 46.dp else 45.dp).background(
                                                brand?.color ?: accentColor
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (brand != null || kind == TileKind.Apps ||
                                                (kind != TileKind.Messages && IconPacks.resolved(app.packageName, app.activityName) != null)) {
                                                LauncherAppIcon(app, Modifier.size(32.dp), contentDescription = null)
                                            } else {
                                                MetroTileIcon(kind, Modifier.size(29.dp))
                                            }
                                        }
                                        Spacer(Modifier.width(if (wp81List) 16.dp else 12.dp))
                                        Text(
                                            app.label,
                                            color = if (wp81List) Color.White else MetroTheme.colors.fg,
                                            fontFamily = MetroRegular,
                                            fontWeight = if (wp81List) FontWeight.Light else FontWeight.Normal,
                                            fontSize = if (wp81List) 24.sp else 18.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    AppListFlyoutMenu(
                                        expanded = expandedMenuKey == app.pinKey(),
                                        pinned = app.pinKey() in pinnedAppIds,
                                        canUninstall = app.canUninstall,
                                        onDismiss = { expandedMenuKey = null },
                                        onTogglePin = {
                                            expandedMenuKey = null
                                            onTogglePinnedApp(app)
                                        },
                                        onUninstall = {
                                            expandedMenuKey = null
                                            onUninstallApp(app)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (wp81List && !wp81SearchOpen && query.isEmpty()) {
                        // WP8.1: round outlined search button in the left gutter, beside the first heading.
                        Box(
                            Modifier.padding(start = 10.dp, top = 12.dp).size(38.dp)
                                .border(2.dp, Color.White, CircleShape)
                                .semantics { contentDescription = "Search apps" }
                                .clickable(enabled = !isExiting) { wp81SearchOpen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(Modifier.size(20.dp)) {
                                val st = 2.dp.toPx()
                                val c = Offset(size.width * 0.58f, size.height * 0.42f)
                                drawCircle(Color.White, radius = size.width * 0.3f, center = c,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(st))
                                drawLine(Color.White, Offset(c.x - size.width * 0.21f, c.y + size.height * 0.21f),
                                    Offset(size.width * 0.1f, size.height * 0.9f), strokeWidth = st * 1.2f)
                            }
                        }
                    }
                    if (appGroups.isEmpty()) {
                        Text(
                            if (query.isBlank()) "No apps found" else "No apps match “${query.trim()}”",
                            color = MetroTheme.colors.subtle,
                            fontFamily = MetroRegular,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = showJumpList,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(170)),
            modifier = Modifier.fillMaxSize()
        ) {
            val w10Picker = startStyle == StartStyle.Windows10Mobile
            MetroAlphabetPicker(
                w10Style = w10Picker,
                // Tint fades; the letters scale in from 0.85 and back out.
                gridModifier = Modifier.animateEnterExit(
                    enter = androidx.compose.animation.scaleIn(tween(220, easing = MetroEnterEasing), initialScale = 0.85f),
                    exit = androidx.compose.animation.scaleOut(tween(170, easing = FastOutSlowInEasing), targetScale = 0.85f)
                ),
                availableSections = groupStartIndices.keys,
                currentSection = groupStartIndices.entries.lastOrNull {
                    it.value <= listState.firstVisibleItemIndex
                }?.key,
                accentColor = accentColor,
                onSelect = { section ->
                    currentSectionStarts[section]?.let { index ->
                        jumpJob?.cancel()
                        jumpJob = scope.launch {
                            if (startStyle == StartStyle.Windows10Mobile) {
                                // Reverse the grid first, then land on the letter.
                                showJumpList = false
                                delay(150)
                                listState.scrollToItem(index)
                            } else {
                                listState.scrollToItem(index)
                                showJumpList = false
                            }
                        }
                    }
                },
                onDismiss = { showJumpList = false }
            )
        }
    }
}
// V20MARK
// V21MARK
// V22MARK
// V22B81MARK
// V23MARK
// V24MARK
// V24BMARK