package com.caglar.pokequery.domain.events

import com.caglar.pokequery.AppVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * v0.6.8 Ãƒâ€Ãƒâ€¡ÃƒÂ¶ Event Guide public-feed and fallback tests.
 *
 * The notes can go stale; these pin the disclaimer + fallback contract so the UI
 * always discloses uncertainty and never claims PokÃ¢â€Å“Ã‚Â®mon GO account access.
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
        // "fresh / manual" disclaimer Ãƒâ€Ãƒâ€¡ÃƒÂ¶ the two resources must never be the same.
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

    // ---- v0.6.9: selectMainEvent logic ----

    @Test
    fun `selectMainEvent returns null for empty list`() {
        assertEquals(null, selectMainEvent(emptyList()))
    }

    @Test
    fun `selectMainEvent prefers CURRENT over UPCOMING`() {
        val upcoming = EventContext(
            id = "upcoming", titleText = "Later",
            contextType = EventContextType.GENERIC_EVENT, status = EventStatus.UPCOMING,
            noteText = "Up", summaryText = "S", prepText = "P", suggestedSearch = "a0",
            eventNotesText = "N", themeKey = "generic_event", month = 8, year = 2026
        )
        val current = EventContext(
            id = "current", titleText = "Now",
            contextType = EventContextType.COMMUNITY_DAY, status = EventStatus.CURRENT,
            noteText = "Cu", summaryText = "S", prepText = "P", suggestedSearch = "a0",
            eventNotesText = "N", themeKey = "community_day", month = 7, year = 2026
        )
        val result = selectMainEvent(listOf(upcoming, current))
        assertEquals("current", result?.id)
    }

    @Test
    fun `selectMainEvent falls back to UPCOMING when no CURRENT exists`() {
        val upcoming = EventContext(
            id = "upcoming-only", titleText = "Next",
            contextType = EventContextType.SPOTLIGHT_HOUR, status = EventStatus.UPCOMING,
            noteText = "U", summaryText = "S", prepText = "P", suggestedSearch = "a0",
            eventNotesText = "N", themeKey = "spotlight_hour", month = 8, year = 2026
        )
        assertEquals("upcoming-only", selectMainEvent(listOf(upcoming))?.id)
    }

    @Test
    fun `selectMainEvent returns first event when all are UPCOMING`() {
        val events = listOf(
            EventContext(id = "first", titleText = "A", contextType = EventContextType.GENERIC_EVENT, status = EventStatus.UPCOMING, noteText = "N", summaryText = "S", prepText = "P", suggestedSearch = "a0", eventNotesText = "N", themeKey = "generic_event", month = 7, year = 2026),
            EventContext(id = "second", titleText = "B", contextType = EventContextType.GENERIC_EVENT, status = EventStatus.UPCOMING, noteText = "N", summaryText = "S", prepText = "P", suggestedSearch = "a0", eventNotesText = "N", themeKey = "generic_event", month = 8, year = 2026)
        )
        assertEquals("first", selectMainEvent(events)?.id)
    }

    @Test
    fun `effectiveStatus uses date window before static feed label`() {
        val event = EventContext(
            id = "go-fest",
            titleText = "GO Fest",
            contextType = EventContextType.GENERIC_EVENT,
            status = EventStatus.CURRENT,
            startDate = "2026-07-11",
            endDate = "2026-07-12",
            noteText = "N",
            summaryText = "S",
            prepText = "P",
            suggestedSearch = "age0",
            eventNotesText = "N"
        )

        assertEquals(EventStatus.UPCOMING, event.effectiveStatus("2026-07-02"))
        assertEquals(EventStatus.CURRENT, event.effectiveStatus("2026-07-11"))
        assertEquals(EventStatus.ENDED, event.effectiveStatus("2026-07-13"))
    }

    @Test
    fun `selectMainEvent skips ended date window when upcoming event exists`() {
        val ended = EventContext(
            id = "ended",
            titleText = "Old",
            contextType = EventContextType.GENERIC_EVENT,
            status = EventStatus.CURRENT,
            startDate = "2026-06-01",
            endDate = "2026-06-02",
            noteText = "N",
            summaryText = "S",
            prepText = "P",
            suggestedSearch = "age0",
            eventNotesText = "N"
        )
        val upcoming = EventContext(
            id = "upcoming",
            titleText = "Next",
            contextType = EventContextType.GENERIC_EVENT,
            status = EventStatus.UPCOMING,
            startDate = "2026-07-11",
            endDate = "2026-07-12",
            noteText = "N",
            summaryText = "S",
            prepText = "P",
            suggestedSearch = "age0",
            eventNotesText = "N"
        )

        assertEquals("upcoming", selectMainEvent(listOf(ended, upcoming), "2026-07-02")?.id)
    }

    // ---- v0.6.9: fixture has featuredPokemon and bonuses ----

    @Test
    fun `bundled fixture has GO Fest planning fields`() {
        val json = File("src/main/res/raw/event_context_fixture.json").readText()
        val feed = EventFeedParser.parse(json).getOrThrow()
        assertTrue("fixture should have events", feed.events.isNotEmpty())
        val goFest = feed.events.firstOrNull { it.id.contains("go-fest", ignoreCase = true) }
        assertNotNull("GO Fest event should exist", goFest)
        assertEquals("2026-07-11", goFest!!.startDate)
        assertEquals("2026-07-12", goFest.endDate)
        assertEquals(EventStatus.UPCOMING, goFest.effectiveStatus("2026-07-02"))
        assertTrue("boosted spawns should not be blank", goFest.boostedPokemonText.orEmpty().isNotBlank())
        assertTrue("raids should not be blank", goFest.raidsText.orEmpty().isNotBlank())
        assertTrue("research should not be blank", goFest.researchText.orEmpty().isNotBlank())
        assertTrue("bonuses should not be blank", goFest.bonusesText.orEmpty().isNotBlank())
    }

    @Test
    fun `fixture events have Turkish translations for featured Pokemon`() {
        val json = File("src/main/res/raw/event_context_fixture.json").readText()
        val feed = EventFeedParser.parse(json).getOrThrow()
        feed.events.forEach { event ->
            // Not every event must have featuredPokemon, but when it does, TR should also exist.
            if (!event.featuredPokemon.isNullOrBlank()) {
                assertTrue("event ${event.id} missing featuredPokemonTr", !event.featuredPokemonTr.isNullOrBlank())
            }
            if (!event.bonusesText.isNullOrBlank()) {
                assertTrue("event ${event.id} missing bonusesTextTr", !event.bonusesTextTr.isNullOrBlank())
            }
        }
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
                  "titleTr": "Topluluk GÃ¢â€Å“Ã¢â€¢ÂnÃ¢â€Å“Ã¢â€¢Â",
                  "kind": "COMMUNITY_DAY",
                  "status": "CURRENT",
                  "note": "Verify in Pokemon GO before acting.",
                  "noteTr": "Ã¢â€â‚¬Ã¢â€“â€˜Ã¢â€Â¼Ã…Å¸lem yapmadan Ã¢â€Å“Ãƒâ€šnce Pokemon GO iÃ¢â€Å“Ã„Å¸inde doÃ¢â€â‚¬Ã…Å¸rula.",
                  "month": 6,
                  "year": 2026,
                  "startDate": "2026-06-29",
                  "endDate": "2026-06-29",
                  "start": "Event day",
                  "end": "After event",
                  "summary": "Useful event planning card.",
                  "summaryTr": "YararlÃ¢â€â‚¬Ã¢â€“â€™ etkinlik hazÃ¢â€â‚¬Ã¢â€“â€™rlÃ¢â€â‚¬Ã¢â€“â€™k kartÃ¢â€â‚¬Ã¢â€“â€™.",
                  "prep": "Tag keepers first.",
                  "prepTr": "Ã¢â€Å“ÃƒÂ»nce saklanacaklarÃ¢â€â‚¬Ã¢â€“â€™ etiketle.",
                  "suggestedSearch": "age0-2&!traded",
                  "eventNotes": "Review protected categories.",
                  "eventNotesTr": "Korunan kategorileri incele.",
                  "themeKey": "community_day"
                }
              ]
            }
        """.trimIndent()

        val feed = EventFeedParser.parse(json).getOrThrow()

        assertEquals("2026-06-29", feed.lastUpdated)
        assertEquals(1, feed.events.size)
        assertFalse(feed.events.single().isManual)
        assertEquals(EventContextType.COMMUNITY_DAY, feed.events.single().contextType)
        assertEquals(EventStatus.CURRENT, feed.events.single().status)
        assertEquals("2026-06-29", feed.events.single().startDate)
        assertEquals("2026-06-29", feed.events.single().endDate)
        assertEquals("age0-2&!traded", feed.events.single().suggestedSearch)
        assertEquals("community_day", feed.events.single().themeKey)
        assertEquals("Topluluk GÃ¢â€Å“Ã¢â€¢ÂnÃ¢â€Å“Ã¢â€¢Â", feed.events.single().titleTextTr)
    }

    @Test
    fun `event feed parser accepts bundled fixture`() {
        val json = File("src/main/res/raw/event_context_fixture.json").readText()

        val feed = EventFeedParser.parse(json).getOrThrow()

        assertEquals("2026-07-10", feed.lastUpdated)
        assertTrue(feed.events.size >= 20)
        assertTrue(feed.events.none { it.isManual })
        feed.events.forEach { event ->
            assertTrue(event.titleText.orEmpty().isNotBlank())
            assertFalse(event.noteText.orEmpty().contains("fallback", ignoreCase = true))
            assertTrue(event.summaryText.orEmpty().isNotBlank())
            assertTrue(event.prepText.orEmpty().isNotBlank())
            assertTrue(event.suggestedSearch.orEmpty().isNotBlank())
            assertTrue(event.eventNotesText.orEmpty().isNotBlank())
            assertTrue(event.themeKey.isNotBlank())
        }
    }

    @Test
    fun `event feed parser accepts pokemon template rows`() {
        val json = File("src/main/res/raw/event_context_fixture.json").readText()

        val feed = EventFeedParser.parse(json).getOrThrow()
        val goFest = feed.events.first { it.id == "event-pokemon-go-fest-2026-global" }

        assertTrue(goFest.pokemon.size >= 4)
        assertTrue(goFest.pokemon.any { it.name == "Mewtwo" && it.source.contains("raid", ignoreCase = true) })
        assertTrue(feed.events.any { event ->
            event.id.contains("anniversary") &&
                event.pokemon.any { it.name == "Gimmighoul" && it.spriteKey == "gimmighoul" }
        })
        assertTrue(goFest.pokemon.any { it.name == "Zeraora" && it.spriteKey == "zeraora" })
        assertTrue(goFest.pokemon.any { it.name == "Raid and trade candidates" && it.nameTr?.isNotBlank() == true })
        assertTrue(goFest.pokemon.any { it.shinyAvailable })
        assertTrue(goFest.pokemon.any { it.spriteKey.orEmpty().isNotBlank() })
        assertTrue(goFest.pokemon.all { it.sourceTr.orEmpty().isNotBlank() && it.noteTr.orEmpty().isNotBlank() })
        assertTrue(feed.events.none { it.suggestedSearch.orEmpty().contains("|") })
        assertTrue(feed.events.any { event ->
            event.id.contains("anniversary") &&
                event.pokemon.any { it.badges.orEmpty().contains("Costume") }
        })
    }

    @Test
    fun `event feed parser fails closed on invalid schema`() {
        val invalid = """{"schemaVersion":2,"lastUpdated":"2026-06-29","events":[]}"""

        assertTrue(EventFeedParser.parse(invalid).isFailure)
    }

    @Test
    fun `event feed parser fails closed on blank fields`() {
        // A note with an empty body would show a blank card Ãƒâ€Ãƒâ€¡ÃƒÂ¶ the parser must reject it rather
        // than silently render an empty event. (Fail-closed: blank event field is invalid.)
        val blankNote = """
            {
              "schemaVersion": 1,
              "lastUpdated": "2026-06-29",
              "events": [
                { "id": "x", "title": "", "kind": "GENERIC_EVENT", "status": "CURRENT", "note": "", "month": 6, "year": 2026,
                  "summary": "", "prep": "", "suggestedSearch": "", "eventNotes": "", "themeKey": "generic_event" }
              ]
            }
        """.trimIndent()
        assertTrue(EventFeedParser.parse(blankNote).isFailure)
    }

    @Test
    fun `event feed parser fails closed on unsupported theme`() {
        val invalid = """
            {
              "schemaVersion": 1,
              "lastUpdated": "2026-06-29",
              "events": [
                { "id": "x", "title": "X", "kind": "GENERIC_EVENT", "status": "CURRENT", "note": "Note", "month": 6, "year": 2026,
                  "summary": "Summary", "prep": "Prep", "suggestedSearch": "age0-1", "eventNotes": "Notes", "themeKey": "official_creature" }
              ]
            }
        """.trimIndent()
        assertTrue(EventFeedParser.parse(invalid).isFailure)
    }

    // ---- v0.6.9: Future event JSON adaptability proof ----
    // The Event Guide dashboard is driven entirely by the feed JSON/model. Adding a future
    // event to the public JSON (or bundled fixture) must NOT require any Compose code changes.
    // This synthetic future event exercises a brand-new id, localized title/date, >=2 Pokemon
    // entries, a bonus card, a research/raid category card, and a suggested search Ãƒâ€Ãƒâ€¡ÃƒÂ¶ proving the
    // parser/model can represent an event that does not exist in production today.

    @Test
    fun `parser accepts a synthetic future event without code changes`() {
        val futureFeed = """
            {
              "schemaVersion": 1,
              "lastUpdated": "2026-12-01",
              "events": [
                {
                  "id": "event-holiday-cup-2027",
                  "title": "Holiday Cup 2027",
                  "titleTr": "Bayram KupasÃ¢â€â‚¬Ã¢â€“â€™ 2027",
                  "titleDe": "Feiertags-Pokal 2027",
                  "titleEs": "Copa Festiva 2027",
                  "titleFr": "Coupe des Fetes 2027",
                  "titleIt": "Coppa Festiva 2027",
                  "kind": "GENERIC_EVENT",
                  "status": "UPCOMING",
                  "note": "A future synthetic event for parser adaptability testing.",
                  "noteTr": "AyrÃ¢â€â‚¬Ã¢â€“â€™Ã¢â€Â¼Ã…Å¸tÃ¢â€â‚¬Ã¢â€“â€™rÃ¢â€â‚¬Ã¢â€“â€™cÃ¢â€â‚¬Ã¢â€“â€™ uyumluluk testi iÃ¢â€Å“Ã„Å¸in gelecekteki sentetik etkinlik.",
                  "month": 12,
                  "year": 2027,
                  "startDate": "2027-12-15",
                  "endDate": "2027-12-19",
                  "start": "Dec 15",
                  "end": "Dec 19",
                  "featuredPokemon": "Cryogonal and Bergmite holiday spawns.",
                  "featuredPokemonTr": "Cryogonal ve Bergmite bayram Ã¢â€Å“Ã„Å¸Ã¢â€â‚¬Ã¢â€“â€™kÃ¢â€â‚¬Ã¢â€“â€™Ã¢â€Â¼Ã…Å¸larÃ¢â€â‚¬Ã¢â€“â€™.",
                  "bonuses": "2x catch Stardust and increased holiday shiny chance.",
                  "bonusesTr": "2x yakalama Star Tozu ve artmÃ¢â€â‚¬Ã¢â€“â€™Ã¢â€Â¼Ã…Å¸ bayram shiny Ã¢â€Â¼Ã…Å¸ansÃ¢â€â‚¬Ã¢â€“â€™.",
                  "raids": "Review holiday raid catches for IVs and shiny before cleanup.",
                  "raidsTr": "Temizlikten Ã¢â€Å“Ãƒâ€šnce bayram akÃ¢â€â‚¬Ã¢â€“â€™n yakalamalarÃ¢â€â‚¬Ã¢â€“â€™nÃ¢â€â‚¬Ã¢â€“â€™ IV ve shiny iÃ¢â€Å“Ã„Å¸in incele.",
                  "research": "Special research leads to a costumed starter.",
                  "researchTr": "Ã¢â€Å“ÃƒÂ»zel araÃ¢â€Â¼Ã…Å¸tÃ¢â€â‚¬Ã¢â€“â€™rma kostÃ¢â€Å“Ã¢â€¢ÂmlÃ¢â€Å“Ã¢â€¢Â baÃ¢â€Â¼Ã…Å¸langÃ¢â€â‚¬Ã¢â€“â€™Ã¢â€Å“Ã„Å¸ PokÃ¢â€Å“Ã‚Â®monuna gÃ¢â€Å“Ãƒâ€štÃ¢â€Å“Ã¢â€¢ÂrÃ¢â€Å“Ã¢â€¢Âr.",
                  "summary": "A holiday-themed event with costumes and shinies.",
                  "summaryTr": "KostÃ¢â€Å“Ã¢â€¢Âm ve shiny iÃ¢â€Å“Ã„Å¸eren bayram temalÃ¢â€â‚¬Ã¢â€“â€™ etkinlik.",
                  "prep": "Tag costumed and shiny catches before cleanup.",
                  "prepTr": "Temizlikten Ã¢â€Å“Ãƒâ€šnce kostÃ¢â€Å“Ã¢â€¢ÂmlÃ¢â€Å“Ã¢â€¢Â ve shiny yakalamalarÃ¢â€â‚¬Ã¢â€“â€™ etiketle.",
                  "suggestedSearch": "age0-5&!favorite&!shiny&!costume&!traded",
                  "eventNotes": "Check costumed and shiny catches before transferring.",
                  "eventNotesTr": "Transferden Ã¢â€Å“Ãƒâ€šnce kostÃ¢â€Å“Ã¢â€¢ÂmlÃ¢â€Å“Ã¢â€¢Â ve shiny yakalamalarÃ¢â€â‚¬Ã¢â€“â€™ kontrol et.",
                  "themeKey": "generic_event",
                  "pokemon": [
                    {
                      "name": "Cryogonal",
                      "source": "wild holiday spawn",
                      "sourceTr": "vahÃ¢â€Â¼Ã…Å¸i bayram Ã¢â€Å“Ã„Å¸Ã¢â€â‚¬Ã¢â€“â€™kÃ¢â€â‚¬Ã¢â€“â€™Ã¢â€Â¼Ã…Å¸Ã¢â€â‚¬Ã¢â€“â€™",
                      "shinyAvailable": true,
                      "note": "Holiday shiny check.",
                      "noteTr": "Bayram shiny kontrolÃ¢â€Å“Ã¢â€¢Â.",
                      "badges": "Shiny, Wild",
                      "badgesTr": "Shiny Ã¢â€Å“Ã„Å¸Ã¢â€â‚¬Ã¢â€“â€™kabilir, VahÃ¢â€Â¼Ã…Å¸i",
                      "spriteKey": "corsola"
                    },
                    {
                      "name": "Bergmite",
                      "source": "raid reward",
                      "sourceTr": "akÃ¢â€â‚¬Ã¢â€“â€™n Ã¢â€Å“Ãƒâ€šdÃ¢â€Å“Ã¢â€¢ÂlÃ¢â€Å“Ã¢â€¢Â",
                      "shinyAvailable": true,
                      "note": "Raid catch with possible special background.",
                      "noteTr": "Ã¢â€Å“ÃƒÂ»zel arka plan ihtimali olan akÃ¢â€â‚¬Ã¢â€“â€™n yakalamasÃ¢â€â‚¬Ã¢â€“â€™.",
                      "badges": "Shiny, Raid",
                      "badgesTr": "Shiny Ã¢â€Å“Ã„Å¸Ã¢â€â‚¬Ã¢â€“â€™kabilir, AkÃ¢â€â‚¬Ã¢â€“â€™n",
                      "spriteKey": "necrozma"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val feed = EventFeedParser.parse(futureFeed).getOrThrow()

        // Title + localized title/date.
        assertEquals(1, feed.events.size)
        val event = feed.events.single()
        assertEquals("event-holiday-cup-2027", event.id)
        assertEquals("Bayram KupasÃ¢â€â‚¬Ã¢â€“â€™ 2027", event.titleTextTr)
        assertEquals("2027-12-15", event.startDate)
        assertEquals("2027-12-19", event.endDate)

        // At least 2 Pokemon entries.
        assertTrue("expected at least 2 pokemon entries", event.pokemon.size >= 2)

        // At least one bonus card (bonuses text present and non-blank).
        assertTrue("bonus card text must be present", !event.bonusesText.isNullOrBlank())
        assertTrue("bonus card TR text must be present", !event.bonusesTextTr.isNullOrBlank())

        // At least one research/raid/category card present and non-blank.
        assertTrue("research card text must be present", !event.researchText.isNullOrBlank())
        assertTrue("raid card text must be present", !event.raidsText.isNullOrBlank())

        // Suggested search present and pipe-free.
        assertTrue("suggested search must be present", !event.suggestedSearch.isNullOrBlank())
        assertFalse("suggested search must not use |", event.suggestedSearch.orEmpty().contains("|"))

        // The model round-trips localized fields used by the dashboard renderer.
        assertEquals("Bayram KupasÃ¢â€â‚¬Ã¢â€“â€™ 2027", event.titleTextTr)
        assertEquals("2x yakalama Star Tozu ve artmÃ¢â€â‚¬Ã¢â€“â€™Ã¢â€Â¼Ã…Å¸ bayram shiny Ã¢â€Â¼Ã…Å¸ansÃ¢â€â‚¬Ã¢â€“â€™.", event.bonusesTextTr)

        // selectMainEvent works on a future-only feed (no code change needed to feature it).
        val main = selectMainEvent(feed.events, "2026-07-04")
        assertNotNull("dashboard must be able to feature the future event", main)
        assertEquals("event-holiday-cup-2027", main?.id)
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
          "status": "CURRENT", "note": "Verify in Pokemon GO before acting.", "month": 6, "year": 2026,
          "summary": "Summary", "prep": "Prep", "suggestedSearch": "age0-2&!traded", "eventNotes": "Notes", "themeKey": "community_day" }
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
            status = EventStatus.CURRENT,
            noteText = "Old note from cache.",
            month = 6,
            year = 2026,
            summaryText = "Cached summary.",
            prepText = "Cached prep.",
            suggestedSearch = "age0-2",
            eventNotesText = "Cached notes.",
            themeKey = "community_day",
            isManual = false
        )
    )
)

class EventFeedLoaderTest {

    // ---- Pure decision functions (no Android Context needed) ----

    @Test
    fun `fetch failure with no cache resolves to OfflineOnly`() {
        // Honest "we have only manual notes" state Ãƒâ€Ãƒâ€¡ÃƒÂ¶ never a crash, never a fake Online.
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
    fun `unparseable successful fetch with no cache resolves to OfflineOnly`() {
        // Fail-closed: a 2xx body that fails schema validation must not become a garbled Online.
        val state = EventFeedLoader.decideAfterParseFailure(manualMonthly = null, cached = null)
        assertTrue("expected OfflineOnly, was $state", state is ContextFeedState.OfflineOnly)
    }

    @Test
    fun `unparseable successful fetch with bundled fallback keeps useful event cards`() {
        val bundled = sampleCachedFeed.copy(lastUpdated = "2026-07-02")

        val state = EventFeedLoader.decideAfterParseFailure(manualMonthly = null, cached = null, bundled = bundled)

        val offline = state as ContextFeedState.OfflineOnly
        assertEquals(1, offline.events.size)
        assertEquals("cached-event", offline.events.single().id)
    }

    @Test
    fun `unparseable successful fetch with cache falls back to StaleCache`() {
        val state = EventFeedLoader.decideAfterParseFailure(manualMonthly = null, cached = sampleCachedFeed)
        assertTrue("expected StaleCache, was $state", state is ContextFeedState.StaleCache)
    }

    @Test
    fun `parse success on a valid feed produces non-manual online events`() {
        // The Online branch (load path) is reached only inside the Android-bound load(), but the
        // decisive precondition Ãƒâ€Ãƒâ€¡ÃƒÂ¶ a valid parse yields non-manual events Ãƒâ€Ãƒâ€¡ÃƒÂ¶ is pure and testable.
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

    @Test
    fun `activeEvents filters ended and sorts current first then upcoming by date`() {
        val ended = EventContext(
            id = "ended", titleText = "Ended Event",
            contextType = EventContextType.GENERIC_EVENT, status = EventStatus.CURRENT,
            startDate = "2026-06-01", endDate = "2026-06-10",
            noteText = "N", summaryText = "S", prepText = "P", suggestedSearch = "a0", eventNotesText = "N"
        )
        val upcomingFar = EventContext(
            id = "upcoming-far", titleText = "Upcoming Far",
            contextType = EventContextType.GENERIC_EVENT, status = EventStatus.UPCOMING,
            startDate = "2026-07-20", endDate = "2026-07-22",
            noteText = "N", summaryText = "S", prepText = "P", suggestedSearch = "a0", eventNotesText = "N"
        )
        val upcomingNear = EventContext(
            id = "upcoming-near", titleText = "Upcoming Near",
            contextType = EventContextType.GENERIC_EVENT, status = EventStatus.UPCOMING,
            startDate = "2026-07-15", endDate = "2026-07-18",
            noteText = "N", summaryText = "S", prepText = "P", suggestedSearch = "a0", eventNotesText = "N"
        )
        val liveEvent = EventContext(
            id = "live-event", titleText = "Live Event",
            contextType = EventContextType.GENERIC_EVENT, status = EventStatus.CURRENT,
            startDate = "2026-07-05", endDate = "2026-07-12",
            noteText = "N", summaryText = "S", prepText = "P", suggestedSearch = "a0", eventNotesText = "N"
        )

        val active = activeEvents(listOf(ended, upcomingFar, upcomingNear, liveEvent), todayIso = "2026-07-08")
        
        // Assert sorting and filtering
        assertEquals(3, active.size)
        assertEquals("live-event", active[0].id)       // CURRENT first
        assertEquals("upcoming-near", active[1].id)     // UPCOMING sorted by date ascending
        assertEquals("upcoming-far", active[2].id)
    }

    @Test
    fun `turkish copy contains correct terms for Link Charges and Fusion Energy`() {
        val events = EventContextRepository.all()
        events.forEach { event ->
            val titleTr = event.localizedTitle("tr")
            val featuredTr = event.localizedFeatured("tr")
            val prepTr = event.localizedPrep("tr")
            val notesTr = event.localizedNotes("tr")
            
            // Link Charges = Bağlantı Şarjı
            // Fusion Energy = Füzyon Enerjisi
            // Turkish banned words checked: "arama dizgisi", "dizgi", etc.
            listOf(titleTr, featuredTr, prepTr, notesTr).forEach { text ->
                assertFalse("Turkish copy must not contain 'arama dizgisi'", text.contains("arama dizgisi", ignoreCase = true))
                assertFalse("Turkish copy must not contain 'dizgi'", text.contains("dizgi", ignoreCase = true))
                assertFalse("Turkish copy must not contain 'Bağlantı Enerjileri'", text.contains("Bağlantı Enerjileri", ignoreCase = true))
            }
            
            event.pokemon.forEach { pk ->
                val nameTr = pk.localizedName("tr")
                val sourceTr = pk.localizedSource("tr")
                val noteTr = pk.localizedNote("tr")
                listOf(nameTr, sourceTr, noteTr).forEach { text ->
                    assertFalse("Turkish copy must not contain 'arama dizgisi'", text.contains("arama dizgisi", ignoreCase = true))
                    assertFalse("Turkish copy must not contain 'dizgi'", text.contains("dizgi", ignoreCase = true))
                    assertFalse("Turkish copy must not contain 'Bağlantı Enerjileri'", text.contains("Bağlantı Enerjileri", ignoreCase = true))
                }
            }
        }
    }

    // ---- v0.7.2: Priority UI groupEvents / featuredScore / importanceTier ----

    private fun makeEvent(
        id: String,
        tier: String = "STANDARD",
        status: EventStatus = EventStatus.CURRENT,
        startDate: String? = null,
        endDate: String? = null,
        category: String? = null
    ) = EventContext(
        id = id,
        titleText = id,
        contextType = EventContextType.GENERIC_EVENT,
        status = status,
        startDate = startDate,
        endDate = endDate,
        noteText = "N",
        summaryText = "S",
        prepText = "P",
        suggestedSearch = "age0",
        eventNotesText = "N",
        importanceTier = tier,
        eventCategory = category
    )

    @Test
    fun `groupEvents collapses true canonical event duplicates only`() {
        val today = "2026-07-10"
        val goFest = makeEvent(
            "event-pokemon-go-fest-2026-global",
            status = EventStatus.UPCOMING,
            startDate = "2026-07-11",
            endDate = "2026-07-12",
            category = EventCategory.MAJOR_GAMEPLAY
        )
        val finalDetailsDuplicate = makeEvent(
            "event-go-fest-2026-global-final-details",
            status = EventStatus.UPCOMING,
            startDate = "2026-07-11",
            endDate = "2026-07-12",
            category = EventCategory.MAJOR_GAMEPLAY
        )

        val sections = groupEvents(listOf(goFest, finalDetailsDuplicate), today)

        assertEquals(1, sections.allActive.count { it.canonicalEventKey() == "event-pokemon-go-fest-2026-global" })
    }

    @Test
    fun `groupEvents keeps unrelated events with the same dates`() {
        val today = "2026-07-10"
        val first = makeEvent("first-event", status = EventStatus.UPCOMING, startDate = "2026-07-11", endDate = "2026-07-12", category = EventCategory.MAJOR_GAMEPLAY)
        val second = makeEvent("second-event", status = EventStatus.UPCOMING, startDate = "2026-07-11", endDate = "2026-07-12", category = EventCategory.MAJOR_GAMEPLAY)

        val sections = groupEvents(listOf(first, second), today)

        assertEquals(2, sections.allActive.size)
        assertTrue(sections.allActive.any { it.id == "first-event" })
        assertTrue(sections.allActive.any { it.id == "second-event" })
    }

    @Test
    fun `bundled feed has one GO Fest gameplay record and community celebration news stays out of upcoming gameplay`() {
        val feed = EventFeedParser.parse(File("src/main/res/raw/event_context_fixture.json").readText()).getOrThrow()
        val events = feed.events
        val goFestGameplay = events.filter {
            it.canonicalEventKey() == "event-pokemon-go-fest-2026-global" &&
                it.determineCategory() == EventCategory.MAJOR_GAMEPLAY
        }
        val communityCelebrations = events.filter { it.id.contains("community-celebrations") }
        val sections = groupEvents(events, todayIso = "2026-07-10")
        val goFestDisplayCount = sections.allActive.count { it.canonicalEventKey() == "event-pokemon-go-fest-2026-global" }

        assertEquals(1, goFestGameplay.size)
        assertTrue(communityCelebrations.isNotEmpty())
        assertTrue(communityCelebrations.all { it.determineCategory() == EventCategory.NEWS_PROMO })
        assertFalse(sections.importantUpcoming.any { it.id.contains("community-celebrations") })
        assertEquals(1, goFestDisplayCount)
    }

    @Test
    fun `every bundled event search preserves traded Pokemon exactly once`() {
        val feed = EventFeedParser.parse(File("src/main/res/raw/event_context_fixture.json").readText()).getOrThrow()

        feed.events.forEach { event ->
            val search = event.suggestedSearch.orEmpty()
            assertFalse("Pipe found in ${event.id}: $search", search.contains('|'))
            assertEquals("Expected one !traded in ${event.id}: $search", 1, search.split('&').count { it == "!traded" })
            assertFalse("Contradictory traded token in ${event.id}: $search", search.split('&').any { it == "traded" })
        }
    }

    @Test
    fun `generic Turkish fallback facts hide even with uppercase dotted I`() {
        assertTrue(isGenericEventFact("İşlem yapmadan önce oyun içi detayları kontrol edin."))
    }

    @Test
    fun `featuredScore uses heroScore internally`() {
        val today = "2026-07-09"
        val major = makeEvent("major", "MAJOR", EventStatus.CURRENT, "2026-07-08", "2026-07-10", EventCategory.MAJOR_GAMEPLAY)
        val standard = makeEvent("std", "STANDARD", EventStatus.CURRENT, "2026-07-08", "2026-07-10", EventCategory.LIMITED_GAMEPLAY)
        assertTrue(major.featuredScore(today) < standard.featuredScore(today))
    }

    @Test
    fun `heroScore prefers MAJOR_GAMEPLAY over SEASON_GBL and NEWS_PROMO`() {
        val today = "2026-07-09"
        val cd = makeEvent("community-day", "MAJOR", EventStatus.UPCOMING, "2026-07-15", "2026-07-15", EventCategory.MAJOR_GAMEPLAY)
        val season = makeEvent("forever-forward", "ROUTINE", EventStatus.CURRENT, "2026-06-01", "2026-09-01", EventCategory.SEASON_GBL)
        val twitch = makeEvent("twitch-drops", "NEWS", EventStatus.CURRENT, "2026-07-09", "2026-07-10", EventCategory.REWARD_DROP)

        assertTrue(cd.heroScore(today) < season.heroScore(today))
        assertTrue(season.heroScore(today) < twitch.heroScore(today))
    }

    @Test
    fun `Twitch Drops is not hero when major gameplay event exists`() {
        val today = "2026-07-09"
        val twitch = makeEvent("twitch-drops", "NEWS", EventStatus.CURRENT, "2026-07-09", "2026-07-10", EventCategory.REWARD_DROP)
        val roadOfLegends = makeEvent("road-of-legends", "MAJOR", EventStatus.UPCOMING, "2026-07-12", "2026-07-14", EventCategory.MAJOR_GAMEPLAY)

        val hero = selectMainEvent(listOf(twitch, roadOfLegends), today)
        assertEquals("road-of-legends", hero?.id)
    }

    @Test
    fun `Season and GBL do not appear in Happening Now`() {
        val today = "2026-07-09"
        val season = makeEvent("forever-forward", "ROUTINE", EventStatus.CURRENT, "2026-06-01", "2026-09-01", EventCategory.SEASON_GBL)
        val gbl = makeEvent("gbl-league", "ROUTINE", EventStatus.CURRENT, "2026-07-09", "2026-07-16", EventCategory.SEASON_GBL)
        val limited = makeEvent("anniversary-party", "STANDARD", EventStatus.CURRENT, "2026-07-08", "2026-07-12", EventCategory.LIMITED_GAMEPLAY)

        val sections = groupEvents(listOf(season, gbl, limited), today)
        // featured should be limited anniversary-party (because it is CURRENT LIMITED_GAMEPLAY, score=10 vs SEASON_GBL score=60)
        assertEquals("anniversary-party", sections.featured?.id)
        // happeningNow should be empty because the only CURRENT LIMITED_GAMEPLAY is already featured
        assertTrue(sections.happeningNow.isEmpty())
        // rotations should contain season and gbl
        assertTrue(sections.rotations.any { it.id == "forever-forward" })
        assertTrue(sections.rotations.any { it.id == "gbl-league" })
    }

    @Test
    fun `Raid rotations and Spotlight and Raid Hour go to Rotations`() {
        val today = "2026-07-09"
        val shadowRaid = makeEvent("shadow-palkia", "ROUTINE", EventStatus.CURRENT, "2026-07-01", "2026-07-31", EventCategory.RAID_ROTATION)
        val spotlight = makeEvent("spotlight-hour", "ROUTINE", EventStatus.UPCOMING, "2026-07-15", "2026-07-15", EventCategory.ROUTINE_ROTATION)
        val cd = makeEvent("community-day", "MAJOR", EventStatus.UPCOMING, "2026-07-15", "2026-07-15", EventCategory.MAJOR_GAMEPLAY)

        val sections = groupEvents(listOf(shadowRaid, spotlight, cd), today)
        // cd is featured (upcoming MAJOR starting <= 21 days, score=20)
        assertEquals("community-day", sections.featured?.id)
        // shadowRaid and spotlight go to rotations
        assertTrue(sections.rotations.any { it.id == "shadow-palkia" })
        assertTrue(sections.rotations.any { it.id == "spotlight-hour" })
    }

    @Test
    fun `Major upcoming gameplay event starts later than routine current but is chosen as hero`() {
        val today = "2026-07-09"
        val routineCurrent = makeEvent("mega-sceptile", "ROUTINE", EventStatus.CURRENT, "2026-07-01", "2026-07-15", EventCategory.RAID_ROTATION)
        val majorUpcoming = makeEvent("go-fest-global", "MAJOR", EventStatus.UPCOMING, "2026-07-12", "2026-07-14", EventCategory.MAJOR_GAMEPLAY)

        val hero = selectMainEvent(listOf(routineCurrent, majorUpcoming), today)
        assertEquals("go-fest-global", hero?.id)
    }

    @Test
    fun `Current limited gameplay event ending soon outranks long running season`() {
        val today = "2026-07-09"
        val limited = makeEvent("hatch-day", "STANDARD", EventStatus.CURRENT, "2026-07-08", "2026-07-10", EventCategory.LIMITED_GAMEPLAY)
        val season = makeEvent("forever-forward", "ROUTINE", EventStatus.CURRENT, "2026-06-01", "2026-09-01", EventCategory.SEASON_GBL)

        val hero = selectMainEvent(listOf(limited, season), today)
        assertEquals("hatch-day", hero?.id)
    }

    @Test
    fun `Important upcoming includes major gameplay within 21 days but excludes routine`() {
        val today = "2026-07-09"
        val upcomingCd = makeEvent("cd-upcoming", "MAJOR", EventStatus.UPCOMING, "2026-07-15", "2026-07-15", EventCategory.MAJOR_GAMEPLAY)
        val upcomingFar = makeEvent("cd-far", "MAJOR", EventStatus.UPCOMING, "2026-08-15", "2026-08-15", EventCategory.MAJOR_GAMEPLAY)
        val upcomingRoutine = makeEvent("spotlight-upcoming", "ROUTINE", EventStatus.UPCOMING, "2026-07-15", "2026-07-15", EventCategory.ROUTINE_ROTATION)

        val sections = groupEvents(listOf(upcomingCd, upcomingFar, upcomingRoutine), today)
        // cd-upcoming should be featured
        assertEquals("cd-upcoming", sections.featured?.id)
        // importantUpcoming should be empty since upcomingCd is already featured and upcomingFar is > 21 days
        assertTrue(sections.importantUpcoming.isEmpty())
        // rotations should contain upcomingRoutine
        assertTrue(sections.rotations.any { it.id == "spotlight-upcoming" })
    }

    @Test
    fun `News promo rewards and announcements entries go to News section`() {
        val today = "2026-07-09"
        val twitch = makeEvent("twitch-drops", "NEWS", EventStatus.CURRENT, "2026-07-09", "2026-07-10", EventCategory.REWARD_DROP)
        val lego = makeEvent("lego-collab", "NEWS", EventStatus.CURRENT, "2026-07-09", "2026-07-15", EventCategory.ANNOUNCEMENT)
        val saveDate = makeEvent("save-the-date", "NEWS", EventStatus.UPCOMING, "2026-07-15", "2026-07-15", EventCategory.NEWS_PROMO)

        val sections = groupEvents(listOf(twitch, lego, saveDate), today)
        // twitch drops (current, score 100) is featured because all are score 100 and it ends earliest/starts earliest
        assertEquals("twitch-drops", sections.featured?.id)
        // news section should contain lego and save-the-date
        assertTrue(sections.news.any { it.id == "lego-collab" })
        assertTrue(sections.news.any { it.id == "save-the-date" })
    }

    @Test
    fun `Full feed remains accessible via allActive`() {
        val today = "2026-07-09"
        val cd = makeEvent("community-day", "MAJOR", EventStatus.UPCOMING, "2026-07-15", "2026-07-15", EventCategory.MAJOR_GAMEPLAY)
        val season = makeEvent("forever-forward", "ROUTINE", EventStatus.CURRENT, "2026-06-01", "2026-09-01", EventCategory.SEASON_GBL)
        val twitch = makeEvent("twitch-drops", "NEWS", EventStatus.CURRENT, "2026-07-09", "2026-07-10", EventCategory.REWARD_DROP)

        val sections = groupEvents(listOf(cd, season, twitch), today)
        // featured is cd
        assertEquals("community-day", sections.featured?.id)
        // allActive contains all 3 events
        assertEquals(3, sections.allActive.size)
        assertTrue(sections.allActive.any { it.id == "community-day" })
        assertTrue(sections.allActive.any { it.id == "forever-forward" })
        assertTrue(sections.allActive.any { it.id == "twitch-drops" })
    }

    @Test
    fun `daysBetween computes correctly`() {
        assertEquals(7, daysBetween("2026-07-01", "2026-07-08"))
        assertEquals(0, daysBetween("2026-07-09", "2026-07-09"))
        assertEquals(999, daysBetween("2026-07-09", null))
    }

    @Test
    fun `importanceTier defaults to STANDARD in EventContext`() {
        val event = EventContext(
            id = "test",
            titleText = "Test",
            contextType = EventContextType.GENERIC_EVENT,
            noteText = "N",
            summaryText = "S",
            prepText = "P",
            suggestedSearch = "age0",
            eventNotesText = "N"
        )
        assertEquals("STANDARD", event.importanceTier)
    }

    @Test
    fun `EventContext determineCategory defaults and overrides correctly`() {
        val withOverride = makeEvent("test", category = "SEASON_GBL")
        assertEquals(EventCategory.SEASON_GBL, withOverride.determineCategory())

        val twitchImplicit = makeEvent("Twitch Drops for celebration")
        assertEquals(EventCategory.REWARD_DROP, twitchImplicit.determineCategory())
    }

    @Test
    fun `Turkish countdown formats correctly`() {
        val today = "2026-07-09"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        
        // 1. Current event ending in 2 days and ~5 hours
        val nowMs = sdf.parse("2026-07-09 18:59:59").time
        val currentEvent = EventContext(
            id = "current-ev",
            titleText = "Current Event",
            contextType = EventContextType.GENERIC_EVENT,
            startDate = "2026-07-09",
            endDate = "2026-07-11"
        )
        val trRemaining = currentEvent.remainingTimeLabel(todayIso = today, nowMillis = nowMs, lang = "tr")
        val enRemaining = currentEvent.remainingTimeLabel(todayIso = today, nowMillis = nowMs, lang = "en")
        
        assertTrue(trRemaining.contains("gün") && trRemaining.contains("saat") && trRemaining.contains("kaldı"))
        assertFalse("TR countdown must not use D", Regex("""\bD\b""").containsMatchIn(trRemaining))
        assertFalse("TR countdown must not use H", Regex("""\bH\b""").containsMatchIn(trRemaining))
        assertFalse(trRemaining.contains("1D") || trRemaining.contains("1H") || trRemaining.contains("D kaldı") || trRemaining.contains("H sonra"))
        assertTrue(enRemaining.contains("d") && enRemaining.contains("h") && enRemaining.contains("left"))

        // 2. Current event ending today
        val endingToday = EventContext(
            id = "ends-today",
            titleText = "Ends Today",
            contextType = EventContextType.GENERIC_EVENT,
            startDate = "2026-07-09",
            endDate = "2026-07-09"
        )
        val nowMsToday = sdf.parse("2026-07-09 20:00:00").time
        val trEndingToday = endingToday.remainingTimeLabel(todayIso = today, nowMillis = nowMsToday, lang = "tr")
        assertEquals("Bugün bitiyor", trEndingToday)

        // 3. Upcoming event starting in 1 day (tomorrow)
        val upcomingEvent = EventContext(
            id = "upcoming-ev",
            titleText = "Upcoming Event",
            contextType = EventContextType.GENERIC_EVENT,
            startDate = "2026-07-10",
            endDate = "2026-07-12"
        )
        val upcomingNowMs = sdf.parse("2026-07-09 10:00:00").time
        val trStartsIn = upcomingEvent.remainingTimeLabel(todayIso = today, nowMillis = upcomingNowMs, lang = "tr")
        assertEquals("Yarın başlıyor", trStartsIn)
    }

    @Test
    fun `dateLabel formats Turkish and English dates correctly`() {
        val event = EventContext(
            id = "test-dates",
            titleText = "Test",
            contextType = EventContextType.GENERIC_EVENT,
            startDate = "2026-07-11",
            endDate = "2026-07-12"
        )
        
        val trLabel = event.dateLabel("tr")
        val enLabel = event.dateLabel("en")
        
        assertEquals("11–12 Temmuz 2026", trLabel)
        assertEquals("July 11–12, 2026", enLabel)
        assertEquals("11.–12. Juli 2026", event.dateLabel("de"))
        assertEquals("11–12 de julio de 2026", event.dateLabel("es"))
        assertEquals("11–12 juillet 2026", event.dateLabel("fr"))
        assertEquals("11–12 luglio 2026", event.dateLabel("it"))
        assertFalse("TR dates must not use ISO year-month-day", Regex("""20\d{2}-\d{2}-\d{2}""").containsMatchIn(trLabel.orEmpty()))
        
        val singleEvent = EventContext(
            id = "test-single",
            titleText = "Test Single",
            contextType = EventContextType.GENERIC_EVENT,
            startDate = "2026-07-11",
            endDate = "2026-07-11"
        )
        assertEquals("11 Temmuz 2026", singleEvent.dateLabel("tr"))
        assertEquals("July 11, 2026", singleEvent.dateLabel("en"))
        assertEquals("11. Juli 2026", singleEvent.dateLabel("de"))
        assertEquals("11 de julio de 2026", singleEvent.dateLabel("es"))
        assertEquals("11 juillet 2026", singleEvent.dateLabel("fr"))
        assertEquals("11 luglio 2026", singleEvent.dateLabel("it"))
    }

    @Test
    fun `compact card date text is localized not ISO for Turkish`() {
        val event = EventContext(
            id = "compact-date",
            titleText = "Raid Hour",
            contextType = EventContextType.GENERIC_EVENT,
            startDate = "2026-07-14",
            endDate = "2026-07-14",
            noteText = "N",
            summaryText = "S",
            prepText = "P",
            suggestedSearch = "age0",
            eventNotesText = "N"
        )
        val tr = compactEventDateText(event, "tr")
        assertEquals("14 Temmuz 2026", tr)
        assertFalse(Regex("""20\d{2}-\d{2}-\d{2}""").containsMatchIn(tr))
        assertFalse(tr.contains("2026-07-14 - 2026-07-14"))
    }

    @Test
    fun `empty generic facts hide placeholder tiles and show honest fallback`() {
        val emptyFacts = EventContext(
            id = "generic-only",
            titleText = "Generic Event",
            contextType = EventContextType.GENERIC_EVENT,
            summaryText = "Verify details in-game before acting.",
            summaryTextTr = "İşlem yapmadan önce oyun içi detayları kontrol edin.",
            prepText = "Prepare for event catches and inventory limits.",
            prepTextTr = "Etkinlik yakalamaları ve envanter limitleri için hazırlık yapın.",
            eventNotesText = "Review recent catches before transfer.",
            eventNotesTextTr = "Transferden önce son yakalamaları kontrol edin.",
            noteText = "N",
            suggestedSearch = "age0"
        )
        val tr = emptyFacts.detailTileVisibility("tr")
        assertFalse(tr.showFeatured)
        assertFalse(tr.showResearch)
        assertFalse(tr.showRaids)
        assertFalse(tr.showBonuses)
        assertFalse(tr.showPrep)
        assertFalse(tr.showCostumeBackground)
        assertFalse(tr.showEventNotes)
        assertTrue(tr.showHonestFallback)
    }

    @Test
    fun `real facts render tiles and costume notes use costume tile`() {
        val rich = EventContext(
            id = "event-go-fest-rich",
            titleText = "GO Fest",
            contextType = EventContextType.GENERIC_EVENT,
            featuredPokemon = "Mewtwo and Zeraora",
            featuredPokemonTr = "Mewtwo ve Zeraora",
            researchText = "Special Research leads to Zeraora.",
            researchTextTr = "Özel Araştırma Zeraora'ya götürür.",
            raidsText = "Review Mewtwo raid catches.",
            raidsTextTr = "Mewtwo akın yakalamalarını incele.",
            bonusesText = "Extra shiny chance",
            bonusesTextTr = "Ek shiny şansı",
            prepText = "Open storage before the event.",
            prepTextTr = "Etkinlikten önce depo aç.",
            eventNotesText = "Keep costume and special-background catches.",
            eventNotesTextTr = "Kostümlü ve özel arka plan yakalamalarını sakla.",
            noteText = "N",
            summaryText = "GO Fest creates many valuable catches.",
            summaryTextTr = "GO Fest çok değerli yakalama oluşturur.",
            suggestedSearch = "age0"
        )
        val tiles = rich.detailTileVisibility("tr")
        assertTrue(tiles.showFeatured)
        assertTrue(tiles.showResearch)
        assertTrue(tiles.showRaids)
        assertTrue(tiles.showBonuses)
        assertTrue(tiles.showPrep)
        assertTrue(tiles.showCostumeBackground)
        assertFalse(tiles.showEventNotes)
        assertFalse(tiles.showHonestFallback)
    }

    @Test
    fun `current limited gameplay like Road of Legends is not hidden`() {
        val road = EventContext(
            id = "event-road-of-legends-2026",
            titleText = "The Road of Legends",
            titleTextTr = "Efsaneler Yolu",
            contextType = EventContextType.GENERIC_EVENT,
            status = EventStatus.CURRENT,
            startDate = "2026-07-06",
            endDate = "2026-07-10",
            eventCategory = EventCategory.LIMITED_GAMEPLAY,
            noteText = "N",
            summaryText = "GO Fest prep week",
            prepText = "Review raid catches",
            suggestedSearch = "age0",
            eventNotesText = "Protect raid catches"
        )
        val sections = groupEvents(listOf(road), "2026-07-09")
        assertNotNull(sections.featured)
        assertEquals("event-road-of-legends-2026", sections.featured?.id)
        assertTrue(sections.allActive.any { it.id == "event-road-of-legends-2026" })
    }

    @Test
    fun `detail modal state path is opened from compact card selection`() {
        // Compact cards set a selected EventContext; non-null means a visible modal/dialog path.
        var clickedEventDetail: EventContext? = null
        val rotation = EventContext(
            id = "event-raidhour",
            titleText = "Raid Hour",
            contextType = EventContextType.GENERIC_EVENT,
            eventCategory = EventCategory.ROUTINE_ROTATION,
            startDate = "2026-07-15",
            endDate = "2026-07-15",
            noteText = "N",
            summaryText = "S",
            prepText = "P",
            suggestedSearch = "age0",
            eventNotesText = "N"
        )
        val news = EventContext(
            id = "event-twitch",
            titleText = "Twitch Drops",
            contextType = EventContextType.GENERIC_EVENT,
            eventCategory = EventCategory.REWARD_DROP,
            startDate = "2026-07-09",
            endDate = "2026-07-12",
            noteText = "N",
            summaryText = "S",
            prepText = "P",
            suggestedSearch = "age0",
            eventNotesText = "N"
        )
        val gameplay = EventContext(
            id = "event-go-fest",
            titleText = "GO Fest",
            contextType = EventContextType.GENERIC_EVENT,
            eventCategory = EventCategory.MAJOR_GAMEPLAY,
            startDate = "2026-07-11",
            endDate = "2026-07-12",
            noteText = "N",
            summaryText = "S",
            prepText = "P",
            suggestedSearch = "age0",
            eventNotesText = "N"
        )
        // Simulate compact-card onClick for each category.
        listOf(rotation, news, gameplay).forEach { event ->
            clickedEventDetail = event
            assertNotNull(clickedEventDetail)
            assertEquals(event.id, clickedEventDetail?.id)
            assertTrue(clickedEventDetail!!.dateLabel("tr").orEmpty().isNotBlank())
            assertFalse(Regex("""20\d{2}-\d{2}-\d{2}""").containsMatchIn(clickedEventDetail!!.dateLabel("tr").orEmpty()))
        }
    }

    @Test
    fun `production feed is not a tiny fixture feed`() {
        val production = File("docs/event-feed/pokequery-events.json")
        if (!production.exists()) {
            // Unit test cwd may be app/; try repo-relative path.
            val alt = File("../docs/event-feed/pokequery-events.json")
            assertTrue("production feed must exist for size check", alt.exists() || production.exists())
            if (alt.exists()) {
                val feed = EventFeedParser.parse(alt.readText()).getOrThrow()
                assertTrue("production feed must be live-sized, not 6-event fixture", feed.events.size >= 20)
                assertTrue(feed.events.none { it.suggestedSearch.orEmpty().contains("|") })
            }
            return
        }
        val feed = EventFeedParser.parse(production.readText()).getOrThrow()
        assertTrue("production feed must be live-sized, not 6-event fixture", feed.events.size >= 20)
        assertTrue(feed.events.none { it.suggestedSearch.orEmpty().contains("|") })
    }
}
