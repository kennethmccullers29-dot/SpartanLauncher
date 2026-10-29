package com.example.spartanlauncher

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

@Composable
internal fun rememberNotificationTileFlip(
    key: String,
    preview: TileNotificationPreview?,
    active: Boolean
): Animatable<Float, AnimationVector1D> {
    val progress = remember(key) { Animatable(0f) }
    // Updating text or artwork should not snap the visible face back to the icon.
    LaunchedEffect(key, preview != null, active) {
        progress.snapTo(0f)
        if (preview != null && active) {
            // Stagger tiles so they never peek in unison (W10M feel).
            delay(1_200L + (key.hashCode().toLong() and 0x7fffffffL) % 4_500L)
            while (true) {
                progress.animateTo(1f, tween(650, easing = androidx.compose.animation.core.CubicBezierEasing(0.1f, 0.9f, 0.2f, 1f)))
                delay(6_000L)
                progress.animateTo(0f, tween(650, easing = androidx.compose.animation.core.CubicBezierEasing(0.1f, 0.9f, 0.2f, 1f)))
                delay(4_000L)
            }
        }
    }
    return progress
}
