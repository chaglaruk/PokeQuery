package com.caglar.pokequery

import com.caglar.pokequery.data.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTest {
    @Test
    fun `bottom tabs route to distinct real screens including history and knowledge`() {
        val destinations = listOf("builder", "favorites", "history", "knowledge", "settings")
            .map(::bottomTabDestination)
        assertTrue(destinations.all { it != null })
        assertEquals(destinations.size, destinations.distinct().size)
        assertEquals(Home, bottomTabDestination("builder"))
        assertEquals(History, bottomTabDestination("history"))
        assertEquals(KnowledgeBase(), bottomTabDestination("knowledge"))
    }

    @Test
    fun `home expert card opens expert builder directly`() {
        assertEquals(ExpertBuilder, homeGoalDestination("expert"))
        assertEquals(Presets, homeGoalDestination("presets"))
        assertEquals(GoalDetail("safe_cleanup"), homeGoalDestination("safe_cleanup"))
    }

    @Test
    fun `direct screenshot routes resolve and old review route falls back home`() {
        assertEquals(GoalDetail("safe_cleanup"), startDestination("detail_safe_cleanup", true))
        assertEquals(History, startDestination("history", true))
        assertEquals(Home, startDestination("review", true))
        assertEquals(Home, startDestination("search", true))
    }

    @Test
    fun `medium and high copies require risk warning`() {
        assertTrue(requiresRiskWarning(RiskLevel.Medium))
        assertTrue(requiresRiskWarning(RiskLevel.High))
        assertFalse(requiresRiskWarning(RiskLevel.Low))
        assertFalse(requiresRiskWarning(RiskLevel.Info))
    }
}
