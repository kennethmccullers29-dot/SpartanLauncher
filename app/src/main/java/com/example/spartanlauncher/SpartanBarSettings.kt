package com.example.spartanlauncher

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal const val SPARTAN_BAR_MODE_KEY = "spartan_bar_auto_connect"
private const val SPARTAN_BAR_PACKAGE = "com.example.spartanbar"
private val BAR_STATE_URI = Uri.parse("content://com.example.spartanbar.state/overlay")

internal object SpartanBarAccess {
    var autoConnect by mutableStateOf(true)
    var enabled by mutableStateOf(false)
    /** Bottom of SpartanBar's top overlays in px (bar, plus call/live strip); 0 = unknown. */
    var insetPx by mutableStateOf(0)
}

/** Observe the actual overlay, rather than hiding system icons merely because the app is installed. */
@Composable
internal fun SpartanBarConnection(active: Boolean) {
    val context = LocalContext.current
    LaunchedEffect(active, SpartanBarAccess.autoConnect) {
        if (!active || !SpartanBarAccess.autoConnect) {
            SpartanBarAccess.enabled = false
            return@LaunchedEffect
        }
        val updates = Channel<Unit>(Channel.CONFLATED)
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { updates.trySend(Unit) }
        }
        var observing = false
        try {
            try {
                context.contentResolver.registerContentObserver(BAR_STATE_URI, false, observer)
                observing = true
            } catch (_: SecurityException) { }
            while (isActive) {
                val state = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.query(BAR_STATE_URI, null, null, null, null)?.use {
                            if (!it.moveToFirst()) null else {
                                val inset = it.getColumnIndex("inset")
                                (it.getInt(it.getColumnIndexOrThrow("active")) == 1) to
                                    (if (inset >= 0) it.getInt(inset) else 0)
                            }
                        }
                    } catch (_: RuntimeException) { null }
                }
                SpartanBarAccess.enabled = state?.first ?: false
                SpartanBarAccess.insetPx = state?.second ?: 0
                // Also recover if the companion process stops without sending a notification.
                withTimeoutOrNull(5_000) { updates.receive() }
            }
        } finally {
            if (observing) context.contentResolver.unregisterContentObserver(observer)
            updates.close()
        }
    }
}

internal fun sendSpartanBarAppearance(context: Context, color: Int) {
    context.sendBroadcast(Intent("com.example.spartanbar.SET_APPEARANCE")
        .setComponent(android.content.ComponentName(SPARTAN_BAR_PACKAGE,
            "$SPARTAN_BAR_PACKAGE.StatusBarAppearanceReceiver"))
        .putExtra("source_package", context.packageName)
        .putExtra("color", color)
        .putExtra("dark_icons", false))
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun Modifier.launcherSystemPadding(scrollBehindStatusBar: Boolean = false): Modifier = windowInsetsPadding(
    if (SpartanBarAccess.enabled && scrollBehindStatusBar)
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
    else if (SpartanBarAccess.enabled)
        WindowInsets.safeDrawing.union(WindowInsets.statusBarsIgnoringVisibility)
    else WindowInsets.safeDrawing
)

/** Start's top clearance belongs inside the scrollable content, so it scrolls away. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun launcherStartTopSpacing(): Dp = if (SpartanBarAccess.enabled) {
    with(LocalDensity.current) {
        maxOf(
            WindowInsets.safeDrawing.getTop(this),
            WindowInsets.statusBarsIgnoringVisibility.getTop(this)
        ).toDp()
    }
} else 0.dp

@Composable
internal fun SpartanBarSettings(accentColor: Color) {
    val context = LocalContext.current
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Connect to SpartanBar", color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp)
                Text(if (SpartanBarAccess.enabled) "Connected · transparent on Start"
                    else "Android’s status bar stays visible until SpartanBar is running.",
                    color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
            }
            MetroToggle(
                checked = SpartanBarAccess.autoConnect,
                onCheckedChange = {
                    context.getSharedPreferences("launcher", Context.MODE_PRIVATE).edit()
                        .putBoolean(SPARTAN_BAR_MODE_KEY, it).apply()
                    SpartanBarAccess.autoConnect = it
                },
                colors = SwitchDefaults.colors(checkedTrackColor = accentColor, checkedThumbColor = Color.White)
            )
        }
        TextButton(onClick = {
            context.packageManager.getLaunchIntentForPackage(SPARTAN_BAR_PACKAGE)?.let(context::startActivity)
        }) { Text("Open SpartanBar", color = accentColor, fontFamily = MetroRegular) }
    }
}
