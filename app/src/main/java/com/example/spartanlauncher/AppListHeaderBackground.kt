package com.example.spartanlauncher

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/** Repaint the stationary wallpaper so scrolling rows cannot show through sticky headers. */
internal fun Modifier.appListHeaderBackground(
    wallpaper: ImageBitmap?,
    rootSize: IntSize,
    backgroundColor: Color,
    shade: State<Float>
): Modifier = composed {
    var position by remember { mutableStateOf(Offset.Zero) }
    onGloballyPositioned { position = it.positionInRoot() }
        .clipToBounds()
        .drawBehind {
            drawRect(backgroundColor)
            if (wallpaper != null && rootSize.width > 0 && rootSize.height > 0) {
                val scale = maxOf(
                    rootSize.width.toFloat() / wallpaper.width,
                    rootSize.height.toFloat() / wallpaper.height
                )
                val width = (wallpaper.width * scale).roundToInt()
                val height = (wallpaper.height * scale).roundToInt()
                drawImage(
                    image = wallpaper,
                    dstOffset = IntOffset(
                        ((rootSize.width - width) / 2f - position.x).roundToInt(),
                        ((rootSize.height - height) / 2f - position.y).roundToInt()
                    ),
                    dstSize = IntSize(width, height)
                )
                // Match the full-screen app list wallpaper shade exactly,
                // including its swipe blend, or headings stick out as bands.
                // Read in the draw phase so every swipe frame repaints.
                drawRect(Color.Black.copy(alpha = shade.value))
            }
        }
}
