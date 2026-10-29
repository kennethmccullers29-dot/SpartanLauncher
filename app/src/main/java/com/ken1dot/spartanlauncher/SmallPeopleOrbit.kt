package com.ken1dot.spartanlauncher

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/** Small W10M People tile: three round faces orbit the centre clockwise; the one passing the back swaps to a new face. */
@Composable
internal fun SmallPeopleOrbit(animationsActive: Boolean, photos: List<PeopleTileContact>, width: Dp, height: Dp) {
    val ids = photos.map { it.id }
    // face >= 0 photo, < 0 translucent filler dot
    var faces by remember(ids) { mutableStateOf(listOf(0, -1, 1 % photos.size)) }
    val turn = remember { Animatable(0f) }
    LaunchedEffect(ids, animationsActive) {
        turn.snapTo(0f)
        if (!animationsActive) return@LaunchedEffect
        var next = 2; var step = 0
        while (true) {
            delay(2_200L + kotlin.random.Random.nextLong(1_800L))
            step++
            turn.animateTo(step.toFloat(), tween(1_100, easing = CubicBezierEasing(0.45f, 0f, 0.15f, 1f)))
            val back = (3 - step % 3) % 3 // slot now sitting at the bottom
            faces = faces.toMutableList().also { it[back] = if (step % 3 == 0) -1 else next++ % photos.size }
        }
    }
    val short = minOf(width, height)
    val d = short * 0.42f
    val r = short * 0.21f
    Box(Modifier.size(width, height).clipToBounds()) {
        faces.forEachIndexed { i, face ->
            val a = Math.toRadians((-90.0 + (i + turn.value) * 120.0))
            val x = width / 2 + r * cos(a).toFloat() - d / 2
            val y = height / 2 + r * sin(a).toFloat() - d / 2 - short * 0.06f
            val photo = photos.getOrNull(face)?.photo
            Box(
                Modifier.offset(x, y).size(d).clip(CircleShape)
                    .then(if (photo == null) Modifier.background(Color.White.copy(alpha = 0.22f)) else Modifier)
            ) { if (photo != null) Image(photo, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        }
    }
}