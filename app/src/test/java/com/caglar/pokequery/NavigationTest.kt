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
    fun `v062 more tools cards map to dedicated non-goal destinations`() {
        listOf("my_presets", "practice", "journal", "events", "presets", "expert", "knowledge", "assistant", "explain").forEach { card ->
            val dest = homeGoalDestination(card)
            assertFalse("More Tools card '$card' must not resolve to GoalDetail", dest is GoalDetail)
        }
    }

    // v0.5.2 (Fix 4): Knowledge Base navigation bug. The Home "Knowledge Base" card must
    // open KnowledgeBase — NOT GoalDetail("knowledge") which the detail screen's unknown-goal
    // fallback routed to Expert Builder. All non-goal Home cards are now explicitly mapped.
    @Test
    fun `home knowledge card opens knowledge base not a goal detail`() {
        val destination = homeGoalDestination("knowledge")
        assertTrue(
            "Home Knowledge Base card must route to KnowledgeBase, got $destination",
            destination is KnowledgeBase
        )
        assertEquals(KnowledgeBase(), destination)
    }

    @Test
    fun `favorites destination still resolves via route but is no longer a home card`() {
        val destination = homeGoalDestination("favorites")
        assertTrue(
            "Favorites route must still resolve to Favorites, got $destination",
            destination is Favorites
        )
    }

    @Test
    fun `every home non-goal card maps to a non-goal-detail destination`() {
        val nonGoalCards = listOf("expert", "presets", "knowledge", "my_presets", "practice", "journal", "events")
        nonGoalCards.forEach { card ->
            val dest = homeGoalDestination(card)
            assertFalse(
                "Home card '$card' must not resolve to GoalDetail (regression: would reopen wrong screen)",
                dest is GoalDetail
            )
        }
    }

    @Test
    fun `direct screenshot routes resolve and old review route falls back home`() {
        assertEquals(GoalDetail("safe_cleanup"), startDestination("detail_safe_cleanup", true))
        assertEquals(History, startDestination("history", true))
        assertEquals(ChangelogRoute, startDestination("changelog", true))
        assertEquals(Home, startDestination("review", true))
        assertEquals(Home, startDestination("search", true))
    }

    // v0.6.1: the new workflow + context surfaces resolve from a start_route (Home cards, app
    // shortcuts and the Quick Access widget all feed startDestination).
    @Test
    fun `v061 and v062 surface routes resolve to their dedicated screens`() {
        assertEquals(MyPresets, startDestination("my_presets", true))
        assertEquals(PracticeMode, startDestination("practice", true))
        assertEquals(CleaningJournal, startDestination("journal", true))
        assertEquals(EventContext, startDestination("events", true))
        assertEquals(SearchAssistant, startDestination("assistant", true))
        assertEquals(ExplainRoute(), startDestination("explain", true))
    }

    @Test
    fun `v061 home cards map to dedicated non-goal destinations`() {
        // None of the new Home cards may resolve to GoalDetail (the KB-bug class of regression).
        listOf("my_presets", "practice", "journal", "events").forEach { card ->
            val dest = homeGoalDestination(card)
            assertFalse(
                "Home card '$card' must not resolve to GoalDetail",
                dest is GoalDetail
            )
        }
        assertEquals(MyPresets, homeGoalDestination("my_presets"))
        assertEquals(PracticeMode, homeGoalDestination("practice"))
        assertEquals(CleaningJournal, homeGoalDestination("journal"))
        assertEquals(EventContext, homeGoalDestination("events"))
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
