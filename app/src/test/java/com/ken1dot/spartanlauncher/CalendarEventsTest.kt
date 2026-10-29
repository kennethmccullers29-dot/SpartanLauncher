package com.ken1dot.spartanlauncher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class CalendarEventsTest {
    @Test fun timedAppointmentExpiresAtItsEnd() {
        val event = CalendarAppointment("Meeting", 1000, 2000, false, "")
        assertTrue(calendarAppointmentIsCurrent(event, 1999))
        assertFalse(calendarAppointmentIsCurrent(event, 2000))
    }

    @Test fun allDayAppointmentUsesLocalExclusiveEndAcrossDaylightSaving() {
        val event = CalendarAppointment("Holiday",
            Instant.parse("2026-03-08T00:00:00Z").toEpochMilli(),
            Instant.parse("2026-03-09T00:00:00Z").toEpochMilli(), true, "")
        val zone = ZoneId.of("America/New_York")
        assertTrue(calendarAppointmentIsCurrent(event,
            Instant.parse("2026-03-09T03:59:59Z").toEpochMilli(), zone))
        assertFalse(calendarAppointmentIsCurrent(event,
            Instant.parse("2026-03-09T04:00:00Z").toEpochMilli(), zone))
    }

    @Test fun allDayAppointmentExpiresAtMidnightInEasternTimeZones() {
        val event = CalendarAppointment("Holiday", 0,
            Instant.parse("2026-10-02T00:00:00Z").toEpochMilli(), true, "")
        val zone = ZoneId.of("Asia/Tokyo")
        assertFalse(calendarAppointmentIsCurrent(event,
            Instant.parse("2026-10-01T15:00:00Z").toEpochMilli(), zone))
    }
}
