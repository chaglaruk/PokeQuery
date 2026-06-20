package com.caglar.pokequery

import com.caglar.pokequery.data.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTest {
    @Test
    fun `bottom tabs route to distinct real screens including knowledge`() {
        val destinations = listOf("home", "builder", "favorites", "knowledge", "settings")
            .map(::bottomTabDestination)
        assertTrue(destinations.all { it != null })
        assertEquals(destinations.size, destinations.distinct().size)
        assertEquals(KnowledgeBase(), bottomTabDestination("knowledge"))
    }

    @Test
    fun `medium and high copies require risk warning`() {
        assertTrue(requiresRiskWarning(RiskLevel.Medium))
        assertTrue(requiresRiskWarning(RiskLevel.High))
        assertFalse(requiresRiskWarning(RiskLevel.Low))
        assertFalse(requiresRiskWarning(RiskLevel.Info))
    }
}
