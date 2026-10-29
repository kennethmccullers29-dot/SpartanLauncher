package com.ken1dot.spartanlauncher

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Accent used by W10M settings controls (provided by LauncherSettingsScreen). */
internal val LocalSettingsAccent = staticCompositionLocalOf { Color(0xFF0078D7) }

/** W10M press tilt: the element leans toward the finger; a swipe cancels the tap. */
internal fun Modifier.metroTilt(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    var press by remember { mutableStateOf<Offset?>(null) }
    var last by remember { mutableStateOf(Offset.Zero) }
    val amount by animateFloatAsState(if (press != null) 1f else 0f, tween(if (press != null) 90 else 180), label = "tilt")
    val slop = LocalViewConfiguration.current.touchSlop
    val click by rememberUpdatedState(onClick)
    this
        .semantics { role = Role.Button; if (enabled) onClick { click(); true } }
        .graphicsLayer {
            val p = press ?: last
            val nx = if (size.width > 0) (p.x / size.width - .5f).coerceIn(-.5f, .5f) else 0f
            val ny = if (size.height > 0) (p.y / size.height - .5f).coerceIn(-.5f, .5f) else 0f
            val strength = if (size.width > 600f) 6f else 12f
            rotationY = nx * strength * amount
            rotationX = -ny * strength * amount
            val s = 1f - 0.03f * amount
            scaleX = s; scaleY = s
            cameraDistance = 12f * density
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                press = down.position; last = down.position
                var tap = true
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if ((change.position - down.position).getDistance() > slop || change.isConsumed) { tap = false; press = null }
                    if (!change.pressed) {
                        press = null
                        if (tap && change.position.x in 0f..size.width.toFloat() && change.position.y in 0f..size.height.toFloat()) { change.consume(); click() }
                        break
                    }
                }
                press = null
            }
        }
}

/** W10M toggle switch: 44x20 pill, white outline + white knob when off, accent fill when on, with On/Off label. */
@Composable
internal fun MetroToggle(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier,
                         enabled: Boolean = true, colors: Any? = null) {
    val accent = LocalSettingsAccent.current
    val knob by animateDpAsState(if (checked) 26.dp else 4.dp, tween(140), label = "knob")
    val fill by animateColorAsState(if (checked) accent else Color.Transparent, tween(140), label = "fill")
    Row(modifier.metroTilt(enabled && onCheckedChange != null) { onCheckedChange?.invoke(!checked) }
        .semantics { stateDescription = if (checked) "On" else "Off"; toggleableState = androidx.compose.ui.state.ToggleableState(checked) }
        .padding(start = 10.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp, 20.dp).background(fill, RoundedCornerShape(10.dp))
            .border(2.dp, if (checked) accent else Color.White, RoundedCornerShape(10.dp))) {
            Box(Modifier.offset(x = knob, y = 5.dp).size(10.dp).background(Color.White, CircleShape))
        }
        Text(if (checked) "On" else "Off", Modifier.padding(start = 10.dp).width(26.dp), color = Color.White,
            fontFamily = MetroRegular, fontSize = 15.sp)
    }
}

/** W10M radio button: 20dp white ring; selected adds an accent ring and dot. */
@Composable
internal fun MetroRadio(selected: Boolean, onClick: (() -> Unit)?, modifier: Modifier = Modifier, colors: Any? = null) {
    val accent = LocalSettingsAccent.current
    Box(modifier.size(28.dp).then(if (onClick != null) Modifier.metroTilt(onClick = onClick) else Modifier), contentAlignment = Alignment.Center) {
        Box(Modifier.size(20.dp).border(2.dp, if (selected) accent else Color.White, CircleShape), contentAlignment = Alignment.Center) {
            if (selected) Box(Modifier.size(10.dp).background(Color.White, CircleShape))
        }
    }
}
internal object SettingsBack { var handler: (() -> Boolean)? = null }
// SETV3
// TILEV3
