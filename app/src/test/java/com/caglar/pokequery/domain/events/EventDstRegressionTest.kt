package com.caglar.pokequery.domain.events

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class EventDstRegressionTest {

    @Test
    fun `event ending on fall-back day remains current through local end of day`() {
        val originalTimeZone = TimeZone.getDefault()
        val newYork = TimeZone.getTimeZone("America/New_York")
        TimeZone.setDefault(newYork)
        try {
            val event = EventContext(
                id = "dst-fallback",
                contextType = EventContextType.GENERIC_EVENT,
                startDate = "2026-11-01",
                endDate = "2026-11-01"
            )
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
                timeZone = newYork
            }
            val nowMillis = requireNotNull(parser.parse("2026-11-01 23:30")).time

            assertEquals(
                "Ends today",
                event.remainingTimeLabel(
                    todayIso = "2026-11-01",
                    nowMillis = nowMillis,
                    lang = "en"
                )
            )
        } finally {
            TimeZone.setDefault(originalTimeZone)
        }
    }
}
