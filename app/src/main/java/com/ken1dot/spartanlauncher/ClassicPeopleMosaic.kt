package com.ken1dot.spartanlauncher

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val FlipOut = CubicBezierEasing(0.6f, 0f, 1f, 1f)
private val FlipIn = CubicBezierEasing(0f, 0f, 0.2f, 1f)

/** WP8.1 People tile grid: medium 3x3, wide 6x3, large 6x6, small 2x2. */
internal fun classicPeopleGrid(w: Float, h: Float): Pair<Int, Int> = when {
    w < 90f && h < 90f -> 2 to 2
    w > h * 1.4f -> 6 to 3
    w > 160f && h > 160f -> 6 to 6
    else -> 3 to 3
}

/**
 * WP8.1 People tile: square contact photos with hairline gaps; one cell at a time flips over its
 * horizontal axis to a new face, and now and then a 2x2 spotlight photo flips in over four cells.
 */
@Composable
internal fun ClassicPeopleMosaic(
    animationsActive: Boolean,
    photos: List<PeopleTileContact>,
    accentColor: Color,
    width: Dp,
    height: Dp
) {
    if (photos.isEmpty()) return
    val (cols, rows) = classicPeopleGrid(width.value, height.value)
    val gap = 0.dp
    val cell = minOf((width - gap * (cols - 1)) / cols, (height - gap * (rows - 1)) / rows)
    val ids = photos.map { it.id }
    // face >= 0: photo index; -1: plain accent cell (like contacts without photos on WP8.1).
    var faces by remember(ids, cols, rows) {
        mutableStateOf(List(cols * rows) { i -> if (photos.size < cols * rows && i % 4 == 3) -1 else i % photos.size })
    }
    val rotations = remember(cols, rows) { List(cols * rows) { Animatable(0f) } }
    var spot by remember(ids, cols, rows) { mutableStateOf<Pair<Int, Int>?>(null) } // (origin cell, photo)
    val spotRot = remember { Animatable(90f) }
    LaunchedEffect(ids, animationsActive, cols, rows) {
        spot = null; spotRot.snapTo(90f)
        if (!animationsActive) return@LaunchedEffect
        var next = cols * rows
        var flips = 0
        delay(1_500L + Random.nextLong(1_500L))
        while (true) {
            if (cols >= 3 && rows >= 3 && flips >= 7 && Random.nextFloat() < 0.35f) {
                flips = 0
                val c = Random.nextInt(cols - 1); val r = Random.nextInt(rows - 1)
                spot = (r * cols + c) to (next++ % photos.size)
                spotRot.snapTo(-90f)
                spotRot.animateTo(0f, tween(420, easing = FlipIn))
                delay(4_500L)
                spotRot.animateTo(90f, tween(320, easing = FlipOut))
                spot = null
            } else {
                val i = Random.nextInt(cols * rows)
                val rot = rotations[i]
                rot.animateTo(90f, tween(260, easing = FlipOut))
                faces = faces.toMutableList().also { it[i] = if (Random.nextInt(9) == 0) -1 else next++ % photos.size }
                rot.snapTo(-90f)
                rot.animateTo(0f, tween(340, easing = FlipIn))
                flips++
            }
            delay(1_400L + Random.nextLong(2_200L))
        }
    }
    Box(Modifier.size(width, height).clipToBounds()) {
        for (i in 0 until cols * rows) {
            val r = i / cols; val c = i % cols
            val face = faces[i]
            Box(
                Modifier.offset(x = (cell + gap) * c, y = (cell + gap) * r).size(cell)
                    .graphicsLayer { rotationX = rotations[i].value; cameraDistance = 8f * density }
                    .then(if (face < 0) Modifier.background(accentColor) else Modifier)
            ) {
                val p = photos.getOrNull(face)?.photo
                if (face >= 0 && p != null) Image(p, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
        spot?.let { (origin, photo) ->
            val r = origin / cols; val c = origin % cols
            val p = photos.getOrNull(photo)?.photo
            Box(
                Modifier.offset(x = (cell + gap) * c, y = (cell + gap) * r).size(cell * 2 + gap)
                    .graphicsLayer { rotationX = spotRot.value; cameraDistance = 8f * density }
                    .then(if (p == null) Modifier.background(accentColor) else Modifier)
            ) { if (p != null) Image(p, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        }
    }
}