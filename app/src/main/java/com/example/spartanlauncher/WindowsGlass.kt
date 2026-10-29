package com.example.spartanlauncher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.FluentMaterials

internal fun useGlassTiles(
    windows10Mobile: Boolean,
    hasWallpaper: Boolean,
    coversOwnFace: Boolean = false,
    enabled: Boolean = true
): Boolean = enabled && windows10Mobile && hasWallpaper && !coversOwnFace

@Composable
internal fun Modifier.windowsGlassTile(
    outline: Color,
    state: HazeState,
    performanceMode: dev.chrisbanes.haze.HazePerformanceMode? = null
): Modifier {
    val acrylic = FluentMaterials.acrylicDefault(isDark = true)
    return this.hazeBlur(input = HazeInput.Backdrop(state), style = acrylic, performanceMode = performanceMode).drawWithContent {
        drawContent()
        val stroke = 1.dp.toPx()
        val inset = stroke
        drawRect(
            color = outline,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            style = Stroke(width = stroke)
        )
    }
}
