package com.ken1dot.spartanlauncher

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun BingWallpaperSettings() {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Bing daily wallpaper", color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp)
            Text("A new background each day while you use the launcher", color = SettingsSecondary,
                fontFamily = MetroRegular, fontSize = 14.sp)
        }
        MetroToggle(checked = BingWallpaperAccess.enabled, onCheckedChange = {
            context.getSharedPreferences("launcher", Context.MODE_PRIVATE).edit().putBoolean(BING_WALLPAPER_ENABLED, it).apply()
            BingWallpaperAccess.enabled = it
        })
    }
    if (BingWallpaperAccess.enabled) {
        BingWallpaperAccess.image?.let {
            Image(it, "Bing daily wallpaper preview", Modifier.fillMaxWidth().height(170.dp), contentScale = ContentScale.Crop)
        }
        if (BingWallpaperAccess.credit.isNotBlank()) Text(BingWallpaperAccess.credit,
            color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 12.sp)
        BingWallpaperAccess.error?.let { Text(it, color = Color(0xFFFFB4AB), fontFamily = MetroRegular, fontSize = 14.sp) }
        TextButton(enabled = !BingWallpaperAccess.loading, onClick = { BingWallpaperAccess.revision++ }) {
            Text(if (BingWallpaperAccess.loading) "Loading wallpaper…" else "Refresh wallpaper", fontFamily = MetroRegular)
        }
    }
}
