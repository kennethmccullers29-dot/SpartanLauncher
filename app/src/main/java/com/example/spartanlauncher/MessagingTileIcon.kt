package com.example.spartanlauncher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class MessageFace { Smile, Wink, Surprised }

internal fun messageFaceFor(count: Int): MessageFace = when {
    count <= 0 -> MessageFace.Smile
    count < 5 -> MessageFace.Wink
    else -> MessageFace.Surprised
}

/**
 * Recreates the rounded, right-tail Windows Phone Messaging tile shown in the
 * reference. The sideways expression is cut out so tile color or wallpaper
 * shows through it.
 */
@Composable
internal fun MessagingTileIcon(
    notificationCount: Int,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = messageFaceFor(notificationCount),
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
        label = "messaging-face",
        modifier = modifier
    ) { face ->
        Canvas(Modifier.fillMaxSize().graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }) {
            scale(size.width / 76f, size.height / 76f, pivot = Offset.Zero) {
                scale(1.3f, 1.3f, pivot = Offset(38f, 38f)) {
                val bubble = Path().apply {
                    moveTo(25.5f, 20f)
                    lineTo(51.5f, 20f)
                    quadraticTo(56f, 20f, 56f, 24.5f)
                    lineTo(56f, 42.5f)
                    quadraticTo(56f, 47f, 51.5f, 47f)
                    lineTo(43f, 47f)
                    lineTo(46f, 56f)
                    lineTo(33f, 47f)
                    lineTo(25.5f, 47f)
                    quadraticTo(21f, 47f, 21f, 42.5f)
                    lineTo(21f, 24.5f)
                    quadraticTo(21f, 20f, 25.5f, 20f)
                    close()
                }
                drawPath(bubble, Color.White)
                drawCircle(Color.Transparent, 1.2f, Offset(32.5f, 30.3f),
                    blendMode = BlendMode.Clear)
                if (face == MessageFace.Wink) {
                    drawLine(Color.Transparent, Offset(31.3f, 38.6f), Offset(33.1f, 35.4f),
                        strokeWidth = 2f, blendMode = BlendMode.Clear)
                } else {
                    drawCircle(Color.Transparent, 1.2f, Offset(32.5f, 37.4f),
                        blendMode = BlendMode.Clear)
                }
                drawLine(Color.Transparent, Offset(36f, 33.8f), Offset(41f, 33.8f),
                    strokeWidth = 1.8f, blendMode = BlendMode.Clear)
                if (face == MessageFace.Surprised) {
                    drawOval(Color.Transparent, topLeft = Offset(43f, 28.5f),
                        size = Size(5.5f, 10f), style = Stroke(width = 1.8f),
                        blendMode = BlendMode.Clear)
                } else {
                    drawArc(Color.Transparent, -74f, 148f,
                        topLeft = Offset(40f, 26f), size = Size(8f, 16f),
                        useCenter = false, style = Stroke(width = 2.3f),
                        blendMode = BlendMode.Clear)
                }
                }
            }
        }
    }
}

@Preview(widthDp = 112, heightDp = 112, showBackground = true, backgroundColor = 0xFFE6CC00)
@Composable
private fun WinkingTilePreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFFE6CC00))) {
        Row(Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically) {
            MessagingTileIcon(1, Modifier.size(68.dp))
            Text("1", Modifier.offset(x = (-10).dp), color = Color.White, fontSize = 34.sp)
        }
        Text("Messaging", Modifier.align(Alignment.BottomStart), color = Color.White, fontSize = 13.sp)
    }
}

@Preview(widthDp = 180, heightDp = 60, showBackground = true, backgroundColor = 0xFF0078D7)
@Composable
private fun MessagingFacesPreview() {
    Row {
        listOf(0, 1, 5).forEach { count ->
            MessagingTileIcon(count, Modifier.size(60.dp))
        }
    }
}
