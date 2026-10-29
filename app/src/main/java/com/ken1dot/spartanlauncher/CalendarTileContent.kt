package com.ken1dot.spartanlauncher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
internal fun CalendarTileContent(date: LocalDate, tileLabel: String, modifier: Modifier = Modifier, active: Boolean = true) {
    val context = LocalContext.current
    val revision = CalendarAccess.revision
    var providerRevision by remember { mutableIntStateOf(0) }
    val allowed = context.checkSelfPermission(android.Manifest.permission.READ_CALENDAR) == android.content.pm.PackageManager.PERMISSION_GRANTED
    DisposableEffect(active, allowed) {
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { providerRevision++ }
        }
        if (active && allowed) try {
            context.contentResolver.registerContentObserver(android.provider.CalendarContract.CONTENT_URI, true, observer)
        } catch (_: SecurityException) { /* Permission may have been revoked. */ }
        onDispose { context.contentResolver.unregisterContentObserver(observer) }
    }
    val appointments by produceState(emptyList<CalendarAppointment>(), active, revision, providerRevision) {
        if (!active || !allowed) { value = emptyList(); return@produceState }
        while (true) {
            value = try { loadUpcomingAppointments(context, limit = 6) }
                catch (_: SecurityException) { emptyList() }
                catch (_: IllegalArgumentException) { emptyList() }
                catch (_: android.database.sqlite.SQLiteException) { emptyList() }
            delay(60_000L)
        }
    }
    CalendarTileFace(date, tileLabel, appointments, modifier, active)
}

@Composable
internal fun CalendarTileFace(
    date: LocalDate,
    tileLabel: String,
    appointments: List<CalendarAppointment>,
    modifier: Modifier = Modifier,
    active: Boolean = false
) {
    var index by remember(appointments) { mutableIntStateOf(0) }
    LaunchedEffect(appointments, active) {
        if (!active || appointments.size < 2) return@LaunchedEffect
        while (true) {
            delay(6_000L)
            index = (index + 1) % appointments.size
        }
    }
    val appointment = appointments.getOrNull(index)
    val locale = Locale.getDefault()
    val weekday = date.format(DateTimeFormatter.ofPattern("EEEE", locale))
    val month = date.format(DateTimeFormatter.ofPattern("MMMM", locale))
    BoxWithConstraints(modifier.clearAndSetSemantics {
        contentDescription = "$tileLabel, " + date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)) +
            appointment?.let { ", ${it.title}, ${calendarAppointmentTime(it)}, ${it.location}" }.orEmpty()
    }) {
        val small = maxHeight < 80.dp
        val wide = maxWidth > maxHeight * 1.4f
        val large = maxHeight >= 220.dp
        val fontScale = LocalDensity.current.fontScale
        val daySize = ((maxHeight.value - if (small) 8f else if (!wide && appointment != null) 20f + 68f * fontScale else 20f + 36f * fontScale) * .85f / fontScale)
            .coerceIn(12f, if (large) 116f else 68f)
        if (wide && !small && !large) {
            // W10M wide face: day label, up to three "time  title" rows, app name.
            val h = maxHeight.value; val w = maxWidth.value
            val text = (h * 0.112f / fontScale).sp
            val pitch = (h * 0.165f).dp
            val is24 = android.text.format.DateFormat.is24HourFormat(LocalContext.current)
            val zone = java.time.ZoneId.systemDefault()
            fun dayOf(e: CalendarAppointment) = java.time.Instant.ofEpochMilli(e.start)
                .atZone(if (e.allDay) java.time.ZoneOffset.UTC else zone).toLocalDate()
            val first = appointments.firstOrNull()
            val day = first?.let { dayOf(it) }
            val rows = if (day == null) emptyList() else appointments.filter { dayOf(it) == day }.take(3)
            val dayLabel = when (day) {
                null -> ""
                LocalDate.now() -> "Today"
                LocalDate.now().plusDays(1) -> "Tomorrow"
                else -> day.format(DateTimeFormatter.ofPattern("EEEE", locale))
            }
            val timeFmt = DateTimeFormatter.ofPattern(if (is24) "HH:mm" else "h:mm a", locale)
            Column(Modifier.fillMaxSize().padding(start = (w * 0.037f).dp, end = (w * 0.03f).dp,
                top = (h * 0.06f).dp, bottom = (h * 0.06f).dp)) {
                if (first == null) {
                    Text("No upcoming events", color = Color.White, fontFamily = MetroRegular,
                        fontSize = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else {
                    Text(dayLabel, color = Color.White, fontFamily = MetroRegular, fontSize = text,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.height(pitch))
                    rows.forEachIndexed { i, e ->
                        val c = if (i == 0) Color.White else Color.White.copy(alpha = .6f)
                        Row(Modifier.height(pitch)) {
                            Text(if (e.allDay) "All day" else java.time.Instant.ofEpochMilli(e.start).atZone(zone).format(timeFmt),
                                color = c, fontFamily = MetroRegular, fontSize = text, maxLines = 1,
                                modifier = Modifier.width((w * 0.29f).dp))
                            Text(e.title, color = c, fontFamily = MetroRegular, fontSize = text,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(tileLabel, color = Color.White, fontFamily = MetroRegular, fontSize = text,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        } else if (small) {
            Text(date.dayOfMonth.toString(), color = Color.White, fontFamily = MetroRegular,
                fontWeight = FontWeight.Light, fontSize = daySize.sp, lineHeight = (daySize * 1.05f).sp,
                maxLines = 1, modifier = Modifier.align(Alignment.Center))
        } else if (!wide && !large) {
            // Medium: no "Calendar" title; weekday over a smaller date number,
            // the pair centered in the middle of the tile.
            val mediumDaySize = daySize * 0.65f
            Box(Modifier.fillMaxSize().padding(8.dp)) {
                Column(Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Text(weekday, color = Color.White, fontFamily = MetroRegular,
                        fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text(date.dayOfMonth.toString(), color = Color.White, fontFamily = MetroRegular,
                        fontWeight = FontWeight.Light, fontSize = mediumDaySize.sp,
                        lineHeight = (mediumDaySize * 1.05f).sp, maxLines = 1)
                }
                AnimatedContent(appointment, transitionSpec = { appointmentSlide() },
                    label = "calendar-appointment",
                    modifier = Modifier.align(Alignment.BottomCenter)) { event ->
                    if (event != null) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(event.title, color = Color.White, fontFamily = MetroRegular,
                            fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text(calendarAppointmentTime(event), color = Color.White,
                            fontFamily = MetroRegular, fontSize = 11.sp, maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        } else {
            Row(Modifier.fillMaxSize().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Column(Modifier.fillMaxHeight().then(if (wide) Modifier.weight(.45f) else Modifier.fillMaxWidth()),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(weekday, color = Color.White, fontFamily = MetroRegular,
                        fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(date.dayOfMonth.toString(), color = Color.White, fontFamily = MetroRegular,
                        fontWeight = FontWeight.Light, fontSize = daySize.sp,
                        lineHeight = (daySize * 1.05f).sp, maxLines = 1,
                        modifier = Modifier.weight(1f))
                    if (!wide) {
                        AnimatedContent(appointment, transitionSpec = { appointmentSlide() }, label = "calendar-appointment") { event ->
                            if (event != null) Column {
                                Text(event.title, color = Color.White, fontFamily = MetroRegular,
                                    fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(calendarAppointmentTime(event), color = Color.White,
                                    fontFamily = MetroRegular, fontSize = 11.sp, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    Text(tileLabel, color = Color.White, fontFamily = MetroRegular,
                        fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (wide) {
                    AnimatedContent(
                        appointment ?: month,
                        Modifier.weight(.55f),
                        transitionSpec = { appointmentSlide() },
                        label = "calendar-wide"
                    ) { face ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val event = face as? CalendarAppointment
                            if (event != null) {
                                Text(event.title, color = Color.White, fontFamily = MetroRegular,
                                    fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(calendarAppointmentTime(event), color = Color.White,
                                    fontFamily = MetroRegular, fontSize = 12.sp, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                                if (event.location.isNotBlank()) Text(event.location,
                                    color = Color.White.copy(alpha = .85f), fontFamily = MetroRegular,
                                    fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            } else {
                                Text(month, color = Color.White, fontFamily = MetroRegular,
                                    fontSize = 22.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(date.year.toString(), color = Color.White.copy(alpha = .85f),
                                    fontFamily = MetroRegular, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun appointmentSlide() =
    (slideInVertically(tween(420)) { it / 2 } + fadeIn(tween(420))) togetherWith
        (slideOutVertically(tween(420)) { -it / 2 } + fadeOut(tween(280)))

internal fun calendarAppointmentTime(event: CalendarAppointment): String {
    val zone = if (event.allDay) java.time.ZoneOffset.UTC else java.time.ZoneId.systemDefault()
    val start = java.time.Instant.ofEpochMilli(event.start).atZone(zone)
    val prefix = if (start.toLocalDate() == LocalDate.now()) "" else
        start.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())) + " · "
    return prefix + if (event.allDay) "All day" else
        start.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
}

@Preview(widthDp = 112, heightDp = 112)
@Composable
private fun CalendarTilePreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFF0078D7))) {
        CalendarTileFace(LocalDate.of(2026, 10, 28), "Calendar", emptyList(), Modifier.fillMaxSize())
    }
}

@Preview(widthDp = 240, heightDp = 112)
@Composable
private fun WideCalendarTilePreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFF0078D7))) {
        CalendarTileFace(LocalDate.now(), "Calendar",
            listOf(CalendarAppointment("Lunch with Alex", System.currentTimeMillis() + 3600000,
                System.currentTimeMillis() + 7200000, false, "Downtown café")), Modifier.fillMaxSize())
    }
}

@Preview(widthDp = 52, heightDp = 52)
@Composable
private fun SmallCalendarTilePreview() = CalendarTilePreview()

@Preview(widthDp = 240, heightDp = 240)
@Composable
private fun LargeCalendarTilePreview() = CalendarTilePreview()

