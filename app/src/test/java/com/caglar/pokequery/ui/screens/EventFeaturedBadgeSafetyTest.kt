package com.caglar.pokequery.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * A shared featured tile must not invent costume or Shiny eligibility.
 * Such claims belong in independently sourced per-event content.
 */
class EventFeaturedBadgeSafetyTest {

    @Test
    fun `neutral featured badges are correctly localized in all six UI languages`() {
        val expected = mapOf(
            "en" to "Review catches",
            "tr" to "Yakalamaları incele",
            "de" to "Fänge prüfen",
            "es" to "Revisar capturas",
            "fr" to "Vérifier les captures",
            "it" to "Controlla le catture"
        )
        for ((lang, expectedBadge) in expected) {
            val badge = eventDashboardLabels(lang).featuredBadge
            assertEquals("Unexpected featured badge for $lang", expectedBadge, badge)
            assertFalse("Featured badge must not assert event-specific Shiny/costume availability: $lang",
                Regex("shiny|costume|kostüm|disfraz", RegexOption.IGNORE_CASE).containsMatchIn(badge))
        }
        assertEquals(expected.getValue("en"), eventDashboardLabels("unknown").featuredBadge)
    }
}
