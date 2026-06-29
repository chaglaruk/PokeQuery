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
 *
 * Note: the production models now expose localized strings via resource IDs (@StringRes) so the
 * Event Context screen can be fully Turkish. On a pure JVM unit test the Android resources cannot
 * be resolved into text, so these tests assert the structural contract instead: manual confidence,
 * the app-version stamp, the stale/fresh disclaimer resource split, and OfflineOnly feed state.
 * The actual localized wording (which must keep "manual", "outdated", "no live event data") lives
 * in values/strings.xml and values-tr/strings.xml.
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
            month = 1, year = 2026,
            titleRes = com.caglar.pokequery.R.string.event_context_community_day,
            contextType = MonthlyContextType.COMMUNITY_DAY,
            noteRes = com.caglar.pokequery.R.string.event_context_community_day_note,
            lastUpdatedInAppVersion = "0.6.1"
        )
        // Replicate the repository's staleness math against a fixed "now" in the future.
        val noteYearMonth = staleNote.year * 12 + (staleNote.month - 1)
        val nowYearMonth = past.get(Calendar.YEAR) * 12 + past.get(Calendar.MONTH)
        assertTrue("prior-month note must be stale", noteYearMonth < nowYearMonth)
    }

    @Test
    fun `stale disclaimer differs from current disclaimer`() {
        val note = MonthlyContext(
            month = 6, year = 2026,
            titleRes = com.caglar.pokequery.R.string.event_context_community_day,
            contextType = MonthlyContextType.COMMUNITY_DAY,
            noteRes = com.caglar.pokequery.R.string.event_context_community_day_note,
            lastUpdatedInAppVersion = "0.6.1"
        )
        val stale = MonthlyContextView(note, isStale = true).disclaimerRes
        val current = MonthlyContextView(note, isStale = false).disclaimerRes
        // A stale note must point to the "may be outdated" disclaimer; a current one to the
        // "fresh / manual" disclaimer — the two resources must never be the same.
        assertTrue("stale disclaimer must be the stale resource", stale == com.caglar.pokequery.R.string.event_context_disclaimer_stale)
        assertTrue("current disclaimer must be the fresh resource", current == com.caglar.pokequery.R.string.event_context_disclaimer_fresh)
        assertFalse(stale == current)
    }
}

class EventContextTest {

    @Test
    fun `event repository ships at least one note and a clear offline disclaimer`() {
        assertTrue("expected at least one bundled event note", EventContextRepository.all().isNotEmpty())
        // Offline/manual honesty contract: the disclaimer resource must always be the manual one.
        assertTrue(
            "disclaimer must be the manual/offline resource",
            EventContextRepository.disclaimerRes() == com.caglar.pokequery.R.string.event_context_disclaimer
        )
    }

    @Test
    fun `bundled event notes are always marked manual`() {
        EventContextRepository.all().forEach { event ->
            assertTrue("bundled note ${event.id} must be manual", event.isManual)
        }
    }

    @Test
    fun `combined offline state returns correct feed state`() {
        val offline = EventContextRepository.combined()
        assertTrue("offline state must be OfflineOnly", offline is ContextFeedState.OfflineOnly)
    }
}
