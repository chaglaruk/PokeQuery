package com.caglar.pokequery.data.repository

import com.caglar.pokequery.data.model.CleaningJournalEntry
import com.caglar.pokequery.data.model.JournalAction
import com.caglar.pokequery.data.model.PersonalPreset
import com.caglar.pokequery.data.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * v0.6.1 — round-trip + corrupt-input tests for the length-prefixed Personal Preset and Journal
 * codecs. Mirrors the SavedTemplateCodec contract: corrupt/partial data decodes to null and is
 * dropped, never crashes. Delimiter characters (':' '&') inside field values are safe because the
 * codec is length-prefixed, not delimiter-split.
 */
class UserContentCodecTest {

    @Test
    fun `personal preset round trips with delimiter characters in every field`() {
        val preset = PersonalPreset(
            id = "id:with:colons",
            title = "My &! preset",
            queryString = "count2-&!shiny&!legendary&!#",
            sourceGoalId = "safe_cleanup",
            sourceFavoriteId = null,
            sourceHistoryId = null,
            riskLevel = RiskLevel.High,
            notes = "note with : & and ! chars",
            createdAt = 1L,
            updatedAt = 2L
        )
        val decoded = PersonalPresetCodec.decode(PersonalPresetCodec.encode(preset))
        assertEquals(preset, decoded)
    }

    @Test
    fun `personal preset with null optionals round trips`() {
        val preset = PersonalPreset(
            id = "x", title = "t", queryString = "0*,1*", sourceGoalId = null,
            sourceFavoriteId = null, sourceHistoryId = null, riskLevel = RiskLevel.Low,
            notes = null, createdAt = 10L, updatedAt = 11L
        )
        assertEquals(preset, PersonalPresetCodec.decode(PersonalPresetCodec.encode(preset)))
    }

    @Test
    fun `personal preset corrupt or partial input decodes to null and never throws`() {
        assertNull(PersonalPresetCodec.decode(""))
        assertNull(PersonalPresetCodec.decode("not-a-record"))
        assertNull(PersonalPresetCodec.decode("5:hello"))           // truncated mid-stream
        assertNull(PersonalPresetCodec.decode("99:hello"))          // length beyond string
        // Bad risk level falls back to Medium rather than crashing the decode.
        val highRisk = PersonalPreset("a", "t", "q", null, null, null, RiskLevel.High, null, 0L, 0L)
        val encoded = PersonalPresetCodec.encode(highRisk).replace("High", "NOPE")
        assertNotNull(PersonalPresetCodec.decode(encoded))
    }

    @Test
    fun `journal entry round trips all action types`() {
        JournalAction.entries.forEach { action ->
            val entry = CleaningJournalEntry(
                id = "j-$action", queryString = "0*,1*&!shiny", title = "Session $action",
                note = "did stuff : & !", actionType = action,
                relatedHistoryId = "h1", relatedFavoriteId = null, relatedPersonalPresetId = "p1",
                createdAt = 5L, updatedAt = 6L
            )
            assertEquals(entry, JournalCodec.decode(JournalCodec.encode(entry)))
        }
    }

    @Test
    fun `journal corrupt input decodes to null`() {
        assertNull(JournalCodec.decode(""))
        assertNull(JournalCodec.decode("garbage"))
        assertNull(JournalCodec.decode("3:abc3:def")) // second length claims 3 but only 3 chars... boundary
    }

    @Test
    fun `journal unknown action name falls back to copied`() {
        val entry = CleaningJournalEntry(
            id = "j", queryString = "q", title = "t", note = "n", actionType = JournalAction.TRADE_SESSION,
            relatedHistoryId = null, relatedFavoriteId = null, relatedPersonalPresetId = null,
            createdAt = 1L, updatedAt = 1L
        )
        // Tamper ONLY the action-name token, keeping the same length so the length-prefix framing
        // stays intact (otherwise decode returns null for a structural reason, not the fallback).
        val bogus = "NOT_AN_ACTION" // same length (13) as "TRADE_SESSION"
        val tampered = JournalCodec.encode(entry).replace(JournalAction.TRADE_SESSION.name, bogus)
        val decoded = JournalCodec.decode(tampered)
        assertNotNull(decoded)
        assertEquals(JournalAction.COPIED, decoded!!.actionType)
    }
}
