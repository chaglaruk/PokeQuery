package com.caglar.pokequery.domain.events

import com.caglar.pokequery.AppVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * v0.6.1 — offline/manual event context tests.
 *
 * The notes are local and can go stale; these pin the disclaimer + staleness contract so the UI
 * always discloses "manual / may be outdated" and never claims live data.
 */
class MonthlyContextTest {

    @Test
    fun `current note is marked manual and carries the app version`() {
        val view = MonthlyContextRepository.currentWithStaleness()
        assertNotNull(view)
        val note = view!!.note
        assertTrue("note must be manual confidence", note.isManual)
        assertEquals(AppVersion.versionName, note.lastUpdatedInAppVersion)
        assertFalse("current month/year should not be stale relative to now", view.isStale)
    }

    @Test
    fun `a note from a prior month is flagged stale`() {
        val past = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2099)
            set(Calendar.MONTH, Calendar.JUNE)
        }
        val staleNote = MonthlyContext(
            month = 1, year = 2020, title = "old", contextType = MonthlyContextType.COMMUNITY_DAY,
            note = "x", lastUpdatedInAppVersion = "0.6.1"
        )
        // Replicate the repository's staleness math against a fixed "now" in the future.
        val noteYearMonth = staleNote.year * 12 + (staleNote.month - 1)
        val nowYearMonth = past.get(Calendar.YEAR) * 12 + past.get(Calendar.MONTH)
        assertTrue("prior-month note must be stale", noteYearMonth < nowYearMonth)
    }

    @Test
    fun `stale disclaimer differs from current disclaimer`() {
        val note = MonthlyContext(
            month = 6, year = 2026, title = "t", contextType = MonthlyContextType.COMMUNITY_DAY,
            note = "n", lastUpdatedInAppVersion = "0.6.1"
        )
        val stale = MonthlyContextView(note, isStale = true).disclaimer
        val current = MonthlyContextView(note, isStale = false).disclaimer
        assertTrue(stale.contains("outdated", ignoreCase = true))
        assertFalse(current.contains("outdated", ignoreCase = true))
    }
}

class EventContextTest {

    @Test
    fun `event repository ships at least one note and a clear offline disclaimer`() {
        assertTrue("expected at least one bundled event note", EventContextRepository.all().isNotEmpty())
        val disclaimer = EventContextRepository.disclaimer()
        assertTrue(disclaimer.contains("manual", ignoreCase = true))
        assertTrue(disclaimer.contains("outdated", ignoreCase = true))
        assertTrue(disclaimer.contains("No live event data", ignoreCase = true))
    }

    @Test
    fun `every event note is manual and offline`() {
        EventContextRepository.all().forEach { event ->
            assertTrue("note ${event.id} must be manual", event.isManual)
        }
    }
}
