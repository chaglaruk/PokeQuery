package com.caglar.pokequery.domain.events

import com.caglar.pokequery.ui.screens.eventInfoSummaryHeading
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventGuideBonusRenderingTest {
    private fun canonicalFeed(): EventFeed {
        val candidate = listOf(
            File("docs/event-feed/pokequery-events.json"),
            File("../docs/event-feed/pokequery-events.json")
        ).first { it.exists() }
        return EventFeedParser.parse(candidate.readText()).getOrThrow()
    }

    @Test
    fun `TCG bonus card uses truthful localized data instead of generic 4x badges`() {
        val tcg = canonicalFeed().events.single {
            it.id == "event-pokemon-tcg-30th-celebration"
        }
        val keywords = mapOf(
            "en" to listOf("Target", "GameStop", "Best Buy", "one per account", "equal value"),
            "tr" to listOf("Target", "GameStop", "Best Buy", "Hesap başına", "eşdeğer"),
            "de" to listOf("Target", "GameStop", "Best Buy", "pro Konto", "gleichwertigen"),
            "es" to listOf("Target", "GameStop", "Best Buy", "por cuenta", "mismo valor"),
            "fr" to listOf("Target", "GameStop", "Best Buy", "par compte", "même valeur"),
            "it" to listOf("Target", "GameStop", "Best Buy", "per account", "pari valore")
        )
        for ((lang, required) in keywords) {
            val tile = tcg.detailTileVisibility(lang)
            assertTrue("bonuses must be shown in $lang", tile.showBonuses)
            for (token in required) {
                assertTrue("$lang missing: $token", tile.bonusesBody.contains(token, ignoreCase = true))
            }
            assertFalse("$lang still has invented 4x gameplay bonus", tile.bonusesBody.contains("4x"))
            assertFalse("$lang still has invented unlimited remote raids", tile.bonusesBody.contains("unlimited Remote"))
        }
    }

    @Test
    fun `bonus detail heading follows app language for all six locales`() {
        assertEquals("Why this matters", eventInfoSummaryHeading("en"))
        assertEquals("Neden önemli?", eventInfoSummaryHeading("tr"))
        assertEquals("Warum das wichtig ist", eventInfoSummaryHeading("de"))
        assertEquals("Por qué importa", eventInfoSummaryHeading("es"))
        assertEquals("Pourquoi c'est important", eventInfoSummaryHeading("fr"))
        assertEquals("Perché è importante", eventInfoSummaryHeading("it"))
    }
}
