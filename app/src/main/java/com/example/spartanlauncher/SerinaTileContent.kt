package com.example.spartanlauncher

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun SerinaTileContent(
    animationsActive: Boolean,
    label: String?,
    modifier: Modifier = Modifier,
) {
    var elapsed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(animationsActive) {
        if (!animationsActive) return@LaunchedEffect
        var previous = 0L
        while (true) {
            withFrameMillis { now ->
                if (previous == 0L) previous = now
                val delta = now - previous
                if (delta >= 32L) {
                    elapsed += delta.coerceAtMost(64L) / 1000f
                    previous = now
                }
            }
        }
    }
    Box(modifier) {
        Canvas(Modifier.fillMaxSize().padding(bottom = if (label != null) 18.dp else 0.dp)) {
            val t = elapsed
            val radius = size.minDimension * 0.33f * (1f + 0.025f * sin(t * PI * 0.7f).toFloat())
            // A brief flourish between long, quiet breathing intervals.
            val cycle = t % 12f
            val tumble = if (cycle > 10f) sin((cycle - 10f) * PI / 2f).toFloat() else 0f
            val phase = (cycle - 10f) * PI.toFloat()
            fun band(outer: Float, width: Float, tilt: Float, color: Color) {
                val inner = outer - width
                val path = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addOval(Rect(center.x - outer, center.y - outer * tilt,
                        center.x + outer, center.y + outer * tilt))
                    addOval(Rect(center.x - inner, center.y - inner * tilt,
                        center.x + inner, center.y + inner * tilt))
                }
                drawPath(path, color)
            }
            rotate(tumble * 30f) {
                band(radius, radius * 0.22f,
                    1f - tumble * (1f - abs(cos(phase)).coerceAtLeast(0.12f)),
                    Color(0xFFD9D9D9).copy(alpha = 0.60f))
            }
            rotate(-tumble * 35f) {
                band(radius * 0.78f, radius * 0.20f,
                    1f - tumble * (1f - abs(sin(phase)).coerceAtLeast(0.12f)),
                    Color.White)
            }
        }
        if (label != null) Text(label, color = Color.White, fontFamily = MetroRegular,
            fontSize = 12.sp, maxLines = 1,
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
    }
}

@Preview(widthDp = 120, heightDp = 120, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SerinaTilePreview() {
    SerinaTileContent(false, "Serina", Modifier.fillMaxSize())
}
