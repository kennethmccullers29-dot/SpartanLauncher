package com.ken1dot.spartanlauncher

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

internal object CalendarAccess {
    var revision by mutableIntStateOf(0)
}

internal data class CalendarAppointment(
    val title: String,
    val start: Long,
    val end: Long,
    val allDay: Boolean,
    val location: String
)

internal suspend fun loadUpcomingAppointments(context: Context, limit: Int = 20): List<CalendarAppointment> =
    withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val today = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val from = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 86400000L
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(uri, from)
        ContentUris.appendId(uri, now + 366 * 86400000L)
        val projection = arrayOf(CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END, CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION)
        context.contentResolver.query(uri.build(), projection,
            "${CalendarContract.Instances.VISIBLE}=1 AND COALESCE(${CalendarContract.Instances.STATUS},0)!=? AND " +
                "COALESCE(${CalendarContract.Instances.SELF_ATTENDEE_STATUS},0)!=?",
            arrayOf(CalendarContract.Events.STATUS_CANCELED.toString(),
                CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED.toString()),
            "${CalendarContract.Instances.BEGIN} ASC")?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val event = CalendarAppointment(cursor.getString(0)?.takeIf { it.isNotBlank() } ?: "Appointment",
                        cursor.getLong(1), cursor.getLong(2), cursor.getInt(3) != 0, cursor.getString(4).orEmpty())
                    if (calendarAppointmentIsCurrent(event, now)) add(event)
                }
            }
        }.orEmpty().sortedWith(compareBy<CalendarAppointment>({ calendarSortKey(it) }, { if (it.allDay) 0 else 1 })).take(limit)
    }

internal fun calendarAppointmentIsCurrent(event: CalendarAppointment, now: Long, zone: ZoneId = ZoneId.systemDefault()): Boolean {
    val ends = if (event.allDay) Instant.ofEpochMilli(event.end).atZone(ZoneId.of("UTC")).toLocalDate()
        .atStartOfDay(zone).toInstant().toEpochMilli() else event.end
    return ends > now
}

internal fun calendarSortKey(e: CalendarAppointment, zone: ZoneId = ZoneId.systemDefault()): Long =
    if (e.allDay) Instant.ofEpochMilli(e.start).atZone(ZoneId.of("UTC")).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli() else e.start
