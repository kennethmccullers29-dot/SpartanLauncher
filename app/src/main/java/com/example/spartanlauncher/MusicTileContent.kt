package com.example.spartanlauncher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@Suppress("UNUSED_PARAMETER")
internal fun MetroMusicGlyph(modifier: Modifier = Modifier, classic: Boolean = false) {
    Canvas(modifier) {
        val s = size.minDimension
        val stroke = s * 0.085f
        drawArc(
            Color.White,
            startAngle = 206f,
            sweepAngle = 128f,
            useCenter = false,
            topLeft = Offset(s * 0.20f, s * 0.08f),
            size = Size(s * 0.60f, s * 0.56f),
            style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        val cup = Size(s * 0.16f, s * 0.34f)
        val radius = androidx.compose.ui.geometry.CornerRadius(s * 0.035f)
        drawRoundRect(Color.White, Offset(s * 0.16f, s * 0.46f), cup, radius)
        drawRoundRect(Color.White, Offset(s * 0.68f, s * 0.46f), cup, radius)
    }
}

@Composable
internal fun MusicTileContent(
    state: MusicTileState,
    tileLabel: String,
    animationsActive: Boolean,
    classic: Boolean,
    modifier: Modifier = Modifier
) {
    // Track changes dissolve; play/pause does not replace the current artwork.
    val face = Triple(state.title, state.artist, state.artwork)
    BoxWithConstraints(modifier.clipToBounds()) {
        val wide = maxWidth > maxHeight * 1.4f
        val large = maxHeight >= 220.dp
        AnimatedContent(
            targetState = face,
            modifier = Modifier.fillMaxSize(),
            contentKey = { it.first to it.second },
            transitionSpec = {
                (fadeIn(tween(450)) togetherWith fadeOut(tween(300))).using(null)
            },
            label = "music-track"
        ) { (title, artist, artwork) ->
            val drift = remember(artwork) { Animatable(0f) }
            LaunchedEffect(animationsActive, state.playing, artwork) {
                if (animationsActive && state.playing && artwork != null) {
                    while (true) {
                        drift.animateTo(1f, tween(18000, easing = LinearEasing))
                        drift.animateTo(0f, tween(18000, easing = LinearEasing))
                    }
                }
            }
            Box(Modifier.fillMaxSize()) {
                artwork?.let {
                    Image(it.asImageBitmap(), null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = 1.08f
                            scaleY = 1.08f
                            translationX = (drift.value - .5f) * size.width * .06f
                        })
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = .12f), Color.Black.copy(alpha = .18f),
                        Color.Black.copy(alpha = if (artwork != null) .88f else .35f))
                )))
                MetroMusicGlyph(
                    Modifier.align(Alignment.TopStart).padding(10.dp).size(if (large) 34.dp else 22.dp),
                    classic
                )
                Column(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (wide || large) {
                        Text(if (state.playing) "NOW PLAYING" else "PAUSED",
                            color = Color.White.copy(alpha = .8f), fontFamily = MetroRegular,
                            fontSize = 10.sp, maxLines = 1)
                    }
                    Text(title ?: tileLabel, color = Color.White, fontFamily = MetroRegular,
                        fontSize = if (large) 26.sp else if (wide) 21.sp else 17.sp,
                        maxLines = if (large) 2 else 1, overflow = TextOverflow.Ellipsis)
                    artist?.let {
                        Text(it, color = Color.White.copy(alpha = .85f), fontFamily = MetroRegular,
                            fontSize = if (large) 17.sp else 13.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(tileLabel, color = Color.White.copy(alpha = .8f), fontFamily = MetroRegular,
                            fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f))
                        MusicPlaybackIndicator(animationsActive && state.playing, state.playing)
                    }
                }
            }
        }
    }
}

@Composable
private fun MusicPlaybackIndicator(active: Boolean, playing: Boolean) {
    val motion = remember { Animatable(0f) }
    LaunchedEffect(active) {
        if (active) {
            while (true) {
                motion.animateTo(1f, tween(650))
                motion.animateTo(0f, tween(650))
            }
        } else motion.snapTo(0f)
    }
    Canvas(Modifier.size(20.dp, 12.dp)) {
        repeat(4) { i ->
            val level = if (!playing) .18f else
                .25f + (if (i % 2 == 0) motion.value else 1f - motion.value) * (.45f + i * .08f)
            val height = size.height * level
            drawRect(Color.White, Offset(i * size.width / 4, size.height - height),
                Size(size.width / 7, height))
        }
    }
}

@Preview(widthDp = 280, heightDp = 140)
@Composable
private fun WideMusicTilePreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFF0078D7))) {
        MusicTileContent(MusicTileState("Midnight City", "M83", null, true),
            "Music", false, false, Modifier.fillMaxSize())
    }
}

@Preview(widthDp = 140, heightDp = 140)
@Composable
private fun MediumMusicTilePreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFF0078D7))) {
        MusicTileContent(MusicTileState("Midnight City", "M83", null, false),
            "Music", false, true, Modifier.fillMaxSize())
    }
}
