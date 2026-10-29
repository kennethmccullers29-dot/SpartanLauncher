package com.ken1dot.spartanlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
internal fun MetroAlphabetPicker(
    availableSections: Set<String>,
    accentColor: Color,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    currentSection: String? = null,
    w10Style: Boolean = false,
    gridModifier: Modifier = Modifier
) {
    if (w10Style) {
        // W10M: full-screen dark tint, plain white letters centred in a
        // 4-column grid; letters with no apps are dimmed.
        Box(
            modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.82f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                gridModifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .pointerInput(Unit) { detectTapGestures { } }
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                appListJumpLetters(availableSections).chunked(4).forEach { letters ->
                    Row {
                        letters.forEach { section ->
                            val available = section in availableSections
                            Box(
                                Modifier.weight(1f).aspectRatio(1.15f)
                                    .semantics { contentDescription = "Jump to $section" }
                                    .clickable(enabled = available, role = Role.Button) { onSelect(section) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    section.uppercase(Locale.ROOT),
                                    color = if (available) Color.White else Color(0xFF5A5A5A),
                                    fontFamily = MetroRegular,
                                    fontSize = 40.sp,
                                    maxLines = 1
                                )
                            }
                        }
                        repeat(4 - letters.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        return
    }
    // WP8.1: solid accent squares over the dimmed list, # a b c ... x y z globe;
    // empty letters are dark gray. Each square flips in with a slight stagger.
    val extras = appListJumpLetters(availableSections).drop(27)
    val cells = listOf("#") + ('a'..'z').map(Char::toString) + "\u0001globe"
    Box(
        modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.72f))
            .clickable(onClick = onDismiss),
        // Centred on screen both ways, with equal side margins.
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            cells.chunked(4).forEachIndexed { rowIdx, letters ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    letters.forEachIndexed { colIdx, cell ->
                        val isGlobe = cell.startsWith("\u0001")
                        val target = if (isGlobe) extras.firstOrNull() else cell.uppercase(Locale.ROOT)
                        val available = target != null && target in availableSections
                        val flip = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay((rowIdx * 22 + colIdx * 12).toLong())
                            flip.animateTo(1f, androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        }
                        Box(
                            Modifier.weight(1f).aspectRatio(1f)
                                .graphicsLayer {
                                    val t = flip.value
                                    rotationX = (1f - t) * -70f
                                    scaleX = 0.8f + 0.2f * t; scaleY = 0.8f + 0.2f * t
                                    alpha = t
                                    cameraDistance = 12f * density
                                }
                                .background(if (available) accentColor else Color(0xFF262626))
                                .semantics { contentDescription = if (isGlobe) "Jump to other scripts" else "Jump to $cell" }
                                .clickable(enabled = available, role = Role.Button) { target?.let(onSelect) },
                            contentAlignment = Alignment.BottomStart
                        ) {
                            if (isGlobe) {
                                androidx.compose.foundation.Canvas(Modifier.padding(start = 10.dp, bottom = 10.dp).size(26.dp)) {
                                    val st = androidx.compose.ui.graphics.drawscope.Stroke(1.6.dp.toPx())
                                    val w = size.width
                                    drawCircle(Color.White, w / 2, style = st)
                                    drawOval(Color.White, androidx.compose.ui.geometry.Offset(w * 0.28f, 0f),
                                        androidx.compose.ui.geometry.Size(w * 0.44f, w), style = st)
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(w / 2, 0f), androidx.compose.ui.geometry.Offset(w / 2, w), 1.6.dp.toPx())
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, w / 2), androidx.compose.ui.geometry.Offset(w, w / 2), 1.6.dp.toPx())
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.1f, w * 0.27f), androidx.compose.ui.geometry.Offset(w * 0.9f, w * 0.27f), 1.6.dp.toPx())
                                    drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.1f, w * 0.73f), androidx.compose.ui.geometry.Offset(w * 0.9f, w * 0.73f), 1.6.dp.toPx())
                                }
                            } else {
                                Text(
                                    cell,
                                    color = Color.White,
                                    fontFamily = MetroRegular,
                                    fontSize = 34.sp,
                                    lineHeight = 34.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(start = 9.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 700, showBackground = true, backgroundColor = 0xFF000000)
@Composable
internal fun MetroAlphabetPickerPreview() {
    MetroAlphabetPicker(
        availableSections = setOf("#", "A", "C", "D", "F", "I", "M", "P", "R", "S", "W"),
        accentColor = Color(0xFF0078D7),
        onSelect = {},
        onDismiss = {}
    )
}
