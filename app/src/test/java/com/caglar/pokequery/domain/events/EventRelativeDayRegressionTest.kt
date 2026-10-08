package com.caglar.pokequery.domain.events

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class EventRelativeDayRegressionTest {

    @Test
    fun `two calendar days away is not tomorrow even if under 48 hours`() {
        val previous = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        try {
            val event = EventContext(
                id = "zorua-2026",
                contextType = EventContextType.COMMUNITY_DAY,
                startDate = "2026-10-10",
                endDate = "2026-10-10"
            )
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val now = requireNotNull(parser.parse("2026-10-08 19:00")).time

            // The calendar date is the 10th, not the 9th. A time interval of
            // 29 hours must not be turned into a misleading "tomorrow" label.
            val labels = mapOf(
                "en" to "in 1d 5h",
                "tr" to "1 gün 5 saat sonra",
                "de" to "in 1 Tg. 5 Std.",
                "es" to "en 1 d. 5 h.",
                "fr" to "dans 1 j. 5 h.",
                "it" to "tra 1 g. 5 o."
            )
            labels.forEach { (lang, expected) ->
                assertEquals(expected, event.remainingTimeLabel(
                    todayIso = "2026-10-08",
                    nowMillis = now,
                    lang = lang
                ))
            }
            assertEquals("Starts tomorrow", event.remainingTimeLabel(
                todayIso = "2026-10-09",
                nowMillis = now + 24 * 60 * 60 * 1000,
                lang = "en"
            ))
        } finally {
            TimeZone.setDefault(previous)
        }
    }
}
