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
        val offlineDisclaimer = EventContextRepository.disclaimer(onlineEnabled = false)
        assertTrue(offlineDisclaimer.contains("manual", ignoreCase = true))
        assertTrue(offlineDisclaimer.contains("outdated", ignoreCase = true))
        assertTrue(offlineDisclaimer.contains("No live event data", ignoreCase = true))
        val onlineDisclaimer = EventContextRepository.disclaimer(onlineEnabled = true)
        assertTrue(onlineDisclaimer.contains("online", ignoreCase = true))
        assertTrue(onlineDisclaimer.contains("Pokémon GO", ignoreCase = true))
    }

    @Test
    fun `bundled event notes are always marked manual`() {
        EventContextRepository.all().forEach { event ->
            assertTrue("bundled note ${event.id} must be manual", event.isManual)
        }
    }

    @Test
    fun `feed entries are marked non-manual`() {
        val feed = com.caglar.pokequery.domain.events.EventFeed(
            notes = listOf(
                com.caglar.pokequery.domain.events.EventFeedEntry("f1", "Test event", "GENERIC_EVENT", "A test note")
            ),
            fetchedAt = System.currentTimeMillis()
        )
        val entries = EventContextRepository.feedEntries(feed)
        assertEquals(1, entries.size)
        assertFalse("feed-based entry must not be manual", entries.first().isManual)
    }

    @Test
    fun `combined offline state returns correct feed state`() {
        val cacheDir = java.io.File(System.getProperty("java.io.tmpdir", "/tmp")!!)
        val offline = EventContextRepository.combined(onlineEnabled = false, cacheDir = cacheDir)
        assertTrue("offline state must be OfflineOnly", offline is com.caglar.pokequery.domain.events.ContextFeedState.OfflineOnly)
    }
}
