package com.example.spartanlauncher

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun TileEditControls(
    label: String,
    resizeLabel: String,
    expanding: Boolean,
    onUnpin: () -> Unit,
    onResize: () -> Unit,
    modifier: Modifier = Modifier,
    // Tiles touching the right screen edge would push their corner buttons
    // off-screen; mirror them to the tile's left corners instead. A tile
    // spanning the full width has no safe side, so keep the buttons just
    // inside its right corners.
    controlsOnStartSide: Boolean = false,
    insetEnd: Boolean = false
) {
    val xShift = when {
        insetEnd -> 2.dp
        controlsOnStartSide -> (-24).dp
        else -> 24.dp
    }
    val unpinAlignment = if (controlsOnStartSide) Alignment.TopStart else Alignment.TopEnd
    val resizeAlignment = if (controlsOnStartSide) Alignment.BottomStart else Alignment.BottomEnd
    Box(modifier.fillMaxSize()) {
        TileEditButton(
            description = "Unpin $label",
            onClick = onUnpin,
            modifier = Modifier.align(unpinAlignment).offset(xShift, (-24).dp)
        ) {
            Canvas(Modifier.size(20.dp)) {
                val stroke = 1.6.dp.toPx()
                rotate(-35f) {
                    val pin = Path().apply {
                        moveTo(size.width * 0.32f, size.height * 0.2f)
                        lineTo(size.width * 0.68f, size.height * 0.2f)
                        lineTo(size.width * 0.61f, size.height * 0.34f)
                        lineTo(size.width * 0.61f, size.height * 0.52f)
                        lineTo(size.width * 0.74f, size.height * 0.65f)
                        lineTo(size.width * 0.26f, size.height * 0.65f)
                        lineTo(size.width * 0.39f, size.height * 0.52f)
                        lineTo(size.width * 0.39f, size.height * 0.34f)
                        close()
                    }
                    drawPath(pin, Color.Black, style = Stroke(stroke))
                    // W10M unpin: the pin with a slash through it.
                    drawLine(Color.Black, Offset(size.width * 0.12f, size.height * 0.5f),
                        Offset(size.width * 0.88f, size.height * 0.5f), stroke, StrokeCap.Round)
                    drawLine(
                        Color.Black,
                        Offset(size.width * 0.5f, size.height * 0.65f),
                        Offset(size.width * 0.5f, size.height * 0.88f),
                        stroke,
                        StrokeCap.Round
                    )
                }
            }
        }
        TileEditButton(
            description = resizeLabel,
            onClick = onResize,
            popDelayMillis = 45,
            modifier = Modifier.align(resizeAlignment).offset(xShift, 24.dp)
        ) {
            Canvas(Modifier.size(20.dp)) {
                val stroke = 1.8.dp.toPx()
                val start = Offset(size.width * 0.25f, size.height * 0.75f)
                val end = Offset(size.width * 0.75f, size.height * 0.25f)
                drawLine(Color.Black, start, end, stroke, StrokeCap.Round)
                val tip = if (expanding) end else start
                val sign = if (expanding) -1f else 1f
                val length = size.width * 0.34f
                drawLine(Color.Black, tip, tip + Offset(sign * length, 0f), stroke, StrokeCap.Round)
                drawLine(Color.Black, tip, tip + Offset(0f, -sign * length), stroke, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun TileEditButton(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier,
    popDelayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    // Buttons pop onto the tile corners with a small spring when edit mode
    // opens, instead of blinking into existence.
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(popDelayMillis.toLong())
        shown = true
    }
    val pop by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 480f),
        label = "edit-button-pop"
    )
    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                val scale = 0.55f + 0.45f * pop
                scaleX = scale
                scaleY = scale
                alpha = pop.coerceIn(0f, 1f)
            }
            .semantics { contentDescription = description }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                // W10M: solid white circle, black glyph.
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
