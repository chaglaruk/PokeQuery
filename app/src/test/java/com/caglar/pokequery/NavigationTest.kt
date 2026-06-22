package com.caglar.pokequery

import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.domain.engine.StringBuilderEngine
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

    // v0.5.1 (Fix 1): Safe Cleanup Copy must route to RiskWarning. Both modes of
    // safe_cleanup now classify as Medium, so requiresRiskWarning(...) is true for the
    // engine output. Info/Low inspection goals (hundo_check, nundo_finder, pvp_candidates)
    // do NOT trigger the warning.
    @Test
    fun `safe cleanup requires risk warning in both modes`() {
        val base = StringBuilderEngine.buildGoal("safe_cleanup")
        val withZero = StringBuilderEngine.buildGoal("safe_cleanup", config = "include0Star")
        assertTrue("safe_cleanup (1*) must route to RiskWarning", requiresRiskWarning(base.riskLevel))
        assertTrue("safe_cleanup (0*,1*) must route to RiskWarning", requiresRiskWarning(withZero.riskLevel))
    }

    @Test
    fun `medium and high risk goals require risk warning`() {
        // Medium
        assertTrue(requiresRiskWarning(StringBuilderEngine.buildGoal("candy_prep").riskLevel))
        assertTrue(requiresRiskWarning(StringBuilderEngine.buildGoal("trade_fodder").riskLevel))
        assertTrue(requiresRiskWarning(StringBuilderEngine.buildGoal("lucky_trade").riskLevel))
        // Info/Low inspection goals do NOT require the warning.
        assertFalse(requiresRiskWarning(StringBuilderEngine.buildGoal("hundo_check").riskLevel))
        assertFalse(requiresRiskWarning(StringBuilderEngine.buildGoal("nundo_finder").riskLevel))
        assertFalse(requiresRiskWarning(StringBuilderEngine.buildGoal("pvp_candidates").riskLevel))
    }
}
