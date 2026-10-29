package com.example.spartanlauncher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun WeatherTileContent(
    state: WeatherTileState,
    modifier: Modifier = Modifier,
    animationsActive: Boolean = false
) {
    BoxWithConstraints(modifier) {
        val wide = maxWidth > maxHeight * 1.4f
        val large = maxHeight >= 220.dp
        val hasDetails = state.feelsLike != null || state.wind != null || state.humidity != null
        val canPeek = animationsActive && hasDetails && (wide || large)
        var page by remember(state.temperature, state.condition, state.feelsLike, state.wind, state.humidity) {
            mutableIntStateOf(0)
        }
        LaunchedEffect(canPeek) {
            if (!canPeek) {
                page = 0
                return@LaunchedEffect
            }
            while (true) {
                delay(7_000L)
                page = 1 - page
            }
        }
        if (state.showBackground && state.temperature != null) WeatherBackdrop(state, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = .45f)))))
        if (wide && state.temperature != null) {
            Row(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(.45f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(state.locationName, color = Color.White, fontFamily = MetroRegular,
                        fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${state.temperature}°", color = Color.White, fontFamily = MetroRegular,
                        fontWeight = FontWeight.Light, fontSize = 34.sp, lineHeight = 38.sp, maxLines = 1)
                    Text("H:${state.high}°  L:${state.low}°", color = Color.White.copy(alpha = .9f),
                        fontFamily = MetroRegular, fontSize = 12.sp, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
                WeatherDetailFace(state, page, canPeek, Modifier.weight(.55f), wide = true, large = true)
            }
        } else Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(state.locationName, color = Color.White, fontFamily = MetroRegular,
                fontSize = if (large) 18.sp else 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(state.temperature?.let { "$it°" } ?: if (state.loading) "Updating weather"
                else state.error ?: "Tap to set up weather",
                color = Color.White, fontFamily = MetroRegular,
                fontWeight = if (state.temperature == null) FontWeight.Normal else FontWeight.Light,
                fontSize = if (state.temperature == null) 16.sp else if (large) 58.sp else 38.sp,
                maxLines = if (state.temperature == null) 3 else 1, overflow = TextOverflow.Ellipsis)
            if (state.temperature != null) {
                WeatherDetailFace(state, page, canPeek, Modifier, wide = false, large = large)
            }
        }
    }
}

@Composable
private fun WeatherDetailFace(
    state: WeatherTileState,
    page: Int,
    canPeek: Boolean,
    modifier: Modifier,
    wide: Boolean,
    large: Boolean
) {
    AnimatedContent(
        targetState = if (canPeek) page else 0,
        modifier = modifier,
        transitionSpec = {
            (slideInVertically(tween(420)) { it / 2 } + fadeIn(tween(420))) togetherWith
                (slideOutVertically(tween(420)) { -it / 2 } + fadeOut(tween(280)))
        },
        label = "weather-detail"
    ) { face ->
        Column(verticalArrangement = Arrangement.spacedBy(if (wide) 3.dp else 2.dp)) {
            if (!canPeek || face == 0) {
                Text(state.condition.orEmpty(), color = Color.White, fontFamily = MetroRegular,
                    fontSize = if (wide) 15.sp else if (large) 20.sp else 14.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!wide) Text("H:${state.high}°  L:${state.low}°",
                    color = Color.White.copy(alpha = .9f), fontFamily = MetroRegular, fontSize = 12.sp)
                if (!canPeek) WeatherExtraLines(state, wide, large, humidity = !wide)
            } else WeatherExtraLines(state, wide, large, humidity = true)
        }
    }
}

@Composable
private fun WeatherExtraLines(state: WeatherTileState, wide: Boolean, large: Boolean, humidity: Boolean) {
    if (wide) {
        state.feelsLike?.let {
            Text("Feels like $it°", color = Color.White.copy(alpha = .85f),
                fontFamily = MetroRegular, fontSize = 12.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
        state.wind?.let {
            Text("Wind $it ${state.units.windUnit}", color = Color.White.copy(alpha = .85f),
                fontFamily = MetroRegular, fontSize = 12.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
        if (humidity) state.humidity?.let {
            Text("Humidity $it%", color = Color.White.copy(alpha = .85f),
                fontFamily = MetroRegular, fontSize = 12.sp)
        }
    } else if (large) {
        val details = listOfNotNull(state.feelsLike?.let { "Feels like $it°" },
            state.wind?.let { "Wind $it ${state.units.windUnit}" })
        Text(details.joinToString(" · "), color = Color.White.copy(alpha = .85f),
            fontFamily = MetroRegular, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        state.humidity?.let { Text("Humidity $it%", color = Color.White.copy(alpha = .85f),
            fontFamily = MetroRegular, fontSize = 14.sp) }
        if (state.error != null) Text("Couldn't refresh · tap to retry", color = Color.White,
            fontFamily = MetroRegular, fontSize = 12.sp)
    }
}

@Composable
private fun WeatherBackdrop(state: WeatherTileState, modifier: Modifier) {
    Canvas(modifier) {
        val code = state.weatherCode
        val rain = code in 51..67 || code in 80..82 || code >= 95
        val snow = code in 71..77 || code in 85..86
        val fog = code == 45 || code == 48
        val overcast = code == 3 || rain || snow || fog
        val night = !state.isDay
        val w = size.width
        val h = size.height
        val sky = when {
            night -> listOf(Color(0xFF091427), Color(0xFF25364D), Color(0xFF526379))
            rain -> listOf(Color(0xFF202B37), Color(0xFF526373), Color(0xFF899299))
            overcast -> listOf(Color(0xFF40586D), Color(0xFF899FAD), Color(0xFFD0D8DC))
            else -> listOf(Color(0xFF07396D), Color(0xFF297CAF), Color(0xFFB0CAD7))
        }
        drawRect(Brush.verticalGradient(sky))
        val light = Offset(w * .82f, h * .16f)
        if (!overcast) {
            val glow = if (night) Color(0xFFC9DCF5) else Color(0xFFFFF1CA)
            drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = .55f),
                glow.copy(alpha = .15f), Color.Transparent), light, h * .7f), h * .7f, light)
            drawCircle(Brush.radialGradient(listOf(glow, glow.copy(alpha = .8f),
                Color.Transparent), light, h * .085f), h * .085f, light)
        }
        if (night && !overcast) repeat(30) { i ->
            drawCircle(Color.White.copy(alpha = .25f + (i % 4) * .12f),
                (.4f + i % 3 * .2f).dp.toPx(),
                Offset(w * (i * 37 % 101) / 101f, h * (i * 23 % 71) / 100f))
        }
        repeat(if (overcast) 36 else if (code in 1..2) 20 else 7) { i ->
            val center = Offset(w * ((i * 31 % 127) / 100f - .12f),
                h * (.22f + (i * 17 % 65) / 100f))
            val radius = h * (.16f + (i % 4) * .045f)
            val shade = if (i % 3 == 0) Color(0xFF263D53) else Color(0xFFE4EDF2)
            val alpha = if (night) .10f else if (overcast) .27f else .18f
            withTransform({ scale(2.1f, .65f, center) }) {
                drawCircle(Brush.radialGradient(listOf(shade.copy(alpha = alpha),
                    shade.copy(alpha = alpha * .55f), Color.Transparent), center, radius), radius, center)
            }
        }
        if (rain) repeat(65) { i ->
            val x = w * (i * 29 % 101) / 100f
            val y = h * (i * 17 % 101) / 100f
            drawLine(Color(0xFFD5E5F0).copy(alpha = .12f + i % 3 * .04f),
                Offset(x, y), Offset(x - h * .035f, y + h * .10f), .65.dp.toPx())
        }
        if (snow) repeat(45) { i ->
            val center = Offset(w * (i * 29 % 101) / 100f, h * (i * 17 % 101) / 100f)
            val radius = (1f + i % 3).dp.toPx()
            drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = .65f),
                Color.Transparent), center, radius), radius, center)
        }
        if (fog) drawRect(Brush.verticalGradient(listOf(Color.Transparent,
            Color(0xFFCBD4DA).copy(alpha = .4f), Color(0xFFACBBC7).copy(alpha = .65f))))
    }
}

@Preview(widthDp = 240, heightDp = 112)
@Composable
private fun WeatherTilePreview() {
    WeatherTileContent(WeatherTileState(temperature = 21, high = 24, low = 15,
        condition = "Partly cloudy", weatherCode = 2, locationName = "London",
        feelsLike = 20, wind = 12), Modifier.fillMaxSize())
}
