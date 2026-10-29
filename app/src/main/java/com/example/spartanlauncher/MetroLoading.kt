package com.example.spartanlauncher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private fun unitPhase(value: Float): Float {
    val remainder = value % 1f
    return if (remainder < 0f) remainder + 1f else remainder
}

@Composable
internal fun MetroAppsLoading(
    accent: Color,
    phoneDots: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.semantics { contentDescription = "Loading apps" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (phoneDots) WindowsPhoneDots(accent) else Windows10ProgressRing(accent)
        Spacer(Modifier.height(22.dp))
        Text(
            "Loading apps",
            color = Color.White.copy(alpha = 0.85f),
            fontFamily = MetroRegular,
            fontWeight = FontWeight.Light,
            fontSize = 22.sp
        )
    }
}

@Composable
private fun WindowsPhoneDots(accent: Color) {
    val phase by rememberInfiniteTransition(label = "wp-dots").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "wp-dot-phase"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            val shifted = (phase - index * 0.12f).let(::unitPhase)
            val pulse = if (shifted < 0.4f) sin((shifted / 0.4f) * PI.toFloat()) else 0f
            val scale = 0.45f + 0.55f * pulse
            Box(
                Modifier
                    .size(12.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = 0.35f + 0.65f * pulse
                    }
                    .background(accent, CircleShape)
            )
        }
    }
}

@Composable
private fun Windows10ProgressRing(accent: Color) {
    val phase by rememberInfiniteTransition(label = "w10-ring").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "w10-ring-phase"
    )
    Canvas(Modifier.size(64.dp)) {
        val count = 6
        val dot = 4.dp.toPx()
        val radius = size.minDimension / 2f - dot
        val center = Offset(size.width / 2f, size.height / 2f)
        repeat(count) { index ->
            val angle = (index.toFloat() / count) * (2f * PI.toFloat()) - PI.toFloat() / 2f
            val shifted = (phase - index.toFloat() / count).let(::unitPhase)
            val alpha = if (shifted < 0.5f) 1f - shifted else 0.2f
            drawCircle(
                color = accent.copy(alpha = alpha.coerceIn(0.2f, 1f)),
                radius = dot,
                center = Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius)
            )
        }
    }
}
