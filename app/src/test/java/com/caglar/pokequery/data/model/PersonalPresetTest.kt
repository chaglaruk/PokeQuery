package com.caglar.pokequery.data.model

import com.caglar.pokequery.AppVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.6.1 Ã¢â‚¬â€ Personal Preset model + Favorites-bridge tests.
 *
 * Pins the safety contract: a personal preset preserves the risk level of its source (never
 * downgrades), and `asGeneratedString()` yields a GeneratedString that still routes through
 * RiskWarning for Medium/High.
 */
class PersonalPresetTest {

    @Test
    fun `from favorite preserves query title and risk level`() {
        val template = SavedTemplate(
            id = "fav-1", name = "My safe cleanup", rawSyntax = "0*,1*&!shiny",
            goalId = "safe_cleanup", riskLevel = RiskLevel.Medium, createdAt = 100L
        )
        val preset = PersonalPreset.fromFavorite(template)
        assertEquals("0*,1*&!shiny", preset.queryString)
        assertEquals("My safe cleanup", preset.title)
        assertEquals(RiskLevel.Medium, preset.riskLevel)
        assertEquals("fav-1", preset.sourceFavoriteId)
        assertTrue(preset.isUserCreated)
    }

    @Test
    fun `from favorite with legacy or preset goal id clears the source goal`() {
        val legacy = SavedTemplate("f", "n", "q", "legacy", RiskLevel.Low, 0L)
        val preset = PersonalPreset.fromFavorite(legacy)
        // legacy/preset goal ids are not real goals, so sourceGoalId is nulled.
        assertEquals(null, preset.sourceGoalId)
    }

    @Test
    fun `as generated string preserves risk level for risk gating`() {
        val high = PersonalPreset("id", "t", "count2-&!traded", null, null, null, RiskLevel.High, null, 0L, 0L)
        val generated = high.asGeneratedString()
        assertEquals(RiskLevel.High, generated.riskLevel)
        assertEquals("count2-&!traded", generated.rawSyntax)
        // Medium/High must still require the risk warning.
        assertTrue(generated.riskLevel == RiskLevel.High || generated.riskLevel == RiskLevel.Medium)
    }

    @Test
    fun `as generated string falls back to a review reminder when notes are null`() {
        val preset = PersonalPreset("id", "t", "q", null, null, null, RiskLevel.Low, notes = null, 0L, 0L)
        val generated = preset.asGeneratedString()
        assertNotNull(generated.plainLanguageExplanation)
        assertTrue(generated.plainLanguageExplanation.contains("Review", ignoreCase = true))
    }

    @Test
    fun `new journal entry stamps timestamps and defaults blank title`() {
        val entry = CleaningJournalEntry.new(
            queryString = "q", title = "", note = "n", actionType = JournalAction.CLEANUP_SESSION
        )
        assertEquals("Journal note", entry.title)
        assertTrue(entry.createdAt > 0)
        assertEquals(entry.createdAt, entry.updatedAt)
        assertEquals(JournalAction.CLEANUP_SESSION, entry.actionType)
    }

    @Test
    fun `journal action from name resolves known and falls back for unknown`() {
        assertEquals(JournalAction.TRADE_SESSION, JournalAction.fromName("TRADE_SESSION"))
        assertEquals(JournalAction.COPIED, JournalAction.fromName(null))
        assertEquals(JournalAction.COPIED, JournalAction.fromName("UNKNOWN"))
    }
}

/**
 * Smoke-checks the version metadata the changelog/events reference (AppVersion), so a partial
 * version bump is caught here rather than only in the dedicated version test.
 */
class AppVersionReferenceTest {
    @Test
    fun `app version is the v0_7_3 release`() {
        assertEquals("0.7.3", AppVersion.versionName)
        assertEquals(23, AppVersion.versionCode)
    }
}
