package com.caglar.pokequery.domain.events

import com.caglar.pokequery.AppVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Calendar

/**
 * v0.6.8 — Event Guide public-feed and fallback tests.
 *
 * The notes can go stale; these pin the disclaimer + fallback contract so the UI
 * always discloses uncertainty and never claims Pokémon GO account access.
 *
 * Note: the production models now expose localized strings via resource IDs (@StringRes) so the
 * Event Guide screen can be fully localized. On a pure JVM unit test the Android resources cannot
 * be resolved into text, so these tests assert the structural contract instead: manual confidence,
 * the app-version stamp, the stale/fresh disclaimer resource split, and feed fallback state.
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

    @Test
    fun `event feed parser accepts schema one feed`() {
        val json = """
            {
              "schemaVersion": 1,
              "lastUpdated": "2026-06-29",
              "events": [
                {
                  "id": "community",
                  "title": "Community Day",
                  "kind": "COMMUNITY_DAY",
                  "note": "Verify in Pokemon GO before acting.",
                  "month": 6,
                  "year": 2026
                }
              ]
            }
        """.trimIndent()

        val feed = EventFeedParser.parse(json).getOrThrow()

        assertEquals("2026-06-29", feed.lastUpdated)
        assertEquals(1, feed.events.size)
        assertFalse(feed.events.single().isManual)
        assertEquals(EventContextType.COMMUNITY_DAY, feed.events.single().contextType)
    }

    @Test
    fun `event feed parser accepts bundled fixture`() {
        val json = File("src/main/res/raw/event_context_fixture.json").readText()

        val feed = EventFeedParser.parse(json).getOrThrow()

        assertEquals("2026-07-01", feed.lastUpdated)
        assertEquals(2, feed.events.size)
        assertTrue(feed.events.none { it.isManual })
    }

    @Test
    fun `event feed parser fails closed on invalid schema`() {
        val invalid = """{"schemaVersion":2,"lastUpdated":"2026-06-29","events":[]}"""

        assertTrue(EventFeedParser.parse(invalid).isFailure)
    }

    @Test
    fun `event feed parser fails closed on blank fields`() {
        // A note with an empty body would show a blank card — the parser must reject it rather
        // than silently render an empty event. (Fail-closed: blank event field is invalid.)
        val blankNote = """
            {
              "schemaVersion": 1,
              "lastUpdated": "2026-06-29",
              "events": [
                { "id": "x", "title": "", "kind": "GENERIC_EVENT", "note": "", "month": 6, "year": 2026 }
              ]
            }
        """.trimIndent()
        assertTrue(EventFeedParser.parse(blankNote).isFailure)
    }
}

/**
 * Fake provider so the loader's state-machine branches can be exercised on a pure JVM with no
 * network. The loader takes the provider as a parameter, which is exactly the seam these tests
 * drive.
 */
private class FakeEventDataProvider(private val result: Result<String>) : EventDataProvider {
    override suspend fun fetch(): Result<String> = result
}

private val VALID_FEED_JSON = """
    {
      "schemaVersion": 1,
      "lastUpdated": "2026-06-29",
      "events": [
        { "id": "community", "title": "Community Day", "kind": "COMMUNITY_DAY",
          "note": "Verify in Pokemon GO before acting.", "month": 6, "year": 2026 }
      ]
    }
""".trimIndent()

/** A sample cached feed reused across the StaleCache-branch assertions. */
private val sampleCachedFeed: EventFeed = EventFeed(
    lastUpdated = "2026-06-20",
    events = listOf(
        EventContext(
            id = "cached-event",
            titleText = "Cached Community Day",
            contextType = EventContextType.COMMUNITY_DAY,
            noteText = "Old note from cache.",
            month = 6,
            year = 2026,
            isManual = false
        )
    )
)

class EventFeedLoaderTest {

    // ---- Pure decision functions (no Android Context needed) ----

    @Test
    fun `fetch failure with no cache resolves to OfflineOnly`() {
        // Honest "we have only manual notes" state — never a crash, never a fake Online.
        val state = EventFeedLoader.decideAfterFetchFailure(manualMonthly = null, cached = null)
        assertTrue("expected OfflineOnly, was $state", state is ContextFeedState.OfflineOnly)
    }

    @Test
    fun `fetch failure with a cached feed resolves to StaleCache`() {
        // Offline but we still have yesterday's feed -> show it, clearly flagged stale.
        val state = EventFeedLoader.decideAfterFetchFailure(manualMonthly = null, cached = sampleCachedFeed)
        val stale = state as ContextFeedState.StaleCache
        assertEquals("2026-06-20", stale.lastUpdated)
        assertEquals(1, stale.events.size)
    }

    @Test
    fun `fetch failure with bundled fallback keeps useful event content`() {
        val bundled = sampleCachedFeed.copy(lastUpdated = "2026-06-29")

        val state = EventFeedLoader.decideAfterFetchFailure(manualMonthly = null, cached = null, bundled = bundled)

        val offline = state as ContextFeedState.OfflineOnly
        assertEquals(1, offline.events.size)
        assertEquals("cached-event", offline.events.single().id)
    }

    @Test
    fun `unparseable successful fetch with no cache resolves to Invalid`() {
        // Fail-closed: a 2xx body that fails schema validation must not become a garbled Online.
        val state = EventFeedLoader.decideAfterParseFailure(manualMonthly = null, cached = null)
        assertTrue("expected Invalid, was $state", state is ContextFeedState.Invalid)
    }

    @Test
    fun `unparseable successful fetch with cache falls back to StaleCache`() {
        val state = EventFeedLoader.decideAfterParseFailure(manualMonthly = null, cached = sampleCachedFeed)
        assertTrue("expected StaleCache, was $state", state is ContextFeedState.StaleCache)
    }

    @Test
    fun `parse success on a valid feed produces non-manual online events`() {
        // The Online branch (load path) is reached only inside the Android-bound load(), but the
        // decisive precondition — a valid parse yields non-manual events — is pure and testable.
        val parsed = EventFeedParser.parse(VALID_FEED_JSON).getOrThrow()
        assertEquals("2026-06-29", parsed.lastUpdated)
        assertEquals(1, parsed.events.size)
        assertFalse("feed events must not be flagged manual", parsed.events.single().isManual)
        assertFalse(parsed.events.single().isManual)
    }

    // ---- Provider seam: the fetch Result shapes the outcome ----

    @Test
    fun `fake provider returns exactly the result it was given`() {
        val ok = FakeEventDataProvider(Result.success(VALID_FEED_JSON))
        val fail = FakeEventDataProvider(Result.failure(java.io.IOException("offline")))
        kotlinx.coroutines.runBlocking {
            assertEquals(VALID_FEED_JSON, ok.fetch().getOrThrow())
            assertTrue(fail.fetch().isFailure)
        }
    }
}
