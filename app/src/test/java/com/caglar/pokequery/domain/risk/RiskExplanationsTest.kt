package com.caglar.pokequery.domain.risk

import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.domain.engine.StringBuilderEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RiskExplanationsTest {

    private val builtInGoals = listOf(
        "safe_cleanup",
        "candy_prep",
        "trade_fodder",
        "hundo_check",
        "untagged",
        "nundo_finder",
        "pvp_candidates",
        "lucky_trade",
        "expert",
        "assistant"
    )

    @Test
    fun `every built-in goal has a valid risk explanation`() {
        builtInGoals.forEach { goalId ->
            val generated = StringBuilderEngine.buildGoal(goalId, customQuery = if (goalId == "expert") "shiny" else "")
            val explanation = RiskExplanations.forGoal(goalId, generated.riskLevel)
            assertEquals(generated.riskLevel, explanation.riskLevel)
            assertTrue(explanation.titleRes != 0)
            assertTrue(explanation.shortReasonRes != 0)
            assertTrue(explanation.safetyChecklistRes.isNotEmpty())
        }
    }

    @Test
    fun `medium goals include action-adjacent language`() {
        listOf("safe_cleanup", "candy_prep", "trade_fodder", "lucky_trade", "expert").forEach { goalId ->
            val generated = StringBuilderEngine.buildGoal(goalId, customQuery = if (goalId == "expert") "shiny" else "")
            val explanation = RiskExplanations.forGoal(goalId, generated.riskLevel)
            assertTrue(explanation.isActionAdjacent)
            assertTrue(explanation.detailedReasonRes != 0)
        }
    }

    @Test
    fun `info goals include inspection-only language`() {
        listOf("hundo_check", "nundo_finder", "pvp_candidates").forEach { goalId ->
            val generated = StringBuilderEngine.buildGoal(goalId)
            val explanation = RiskExplanations.forGoal(goalId, generated.riskLevel)
            assertEquals(RiskLevel.Info, generated.riskLevel)
            assertTrue(explanation.isInspectionOnly)
            assertTrue(explanation.titleRes != 0)
        }
    }

    @Test
    fun `related knowledge ids exist in shipped local knowledge base`() {
        val json = listOf(
            File("app/src/main/assets/knowledgebase.json"),
            File("src/main/assets/knowledgebase.json")
        ).first { it.exists() }.readText()
        RiskExplanations.all.flatMap { it.relatedKnowledgeIds }.distinct().forEach { id ->
            assertTrue("Missing knowledge id $id", json.contains("\"id\": \"$id\""))
        }
    }

    @Test
    fun `risk level values are not changed accidentally`() {
        assertEquals(RiskLevel.Medium, StringBuilderEngine.buildGoal("safe_cleanup").riskLevel)
        assertEquals(RiskLevel.Medium, StringBuilderEngine.buildGoal("candy_prep").riskLevel)
        assertEquals(RiskLevel.Medium, StringBuilderEngine.buildGoal("trade_fodder").riskLevel)
        assertEquals(RiskLevel.Medium, StringBuilderEngine.buildGoal("lucky_trade").riskLevel)
        assertEquals(RiskLevel.Info, StringBuilderEngine.buildGoal("hundo_check").riskLevel)
        assertEquals(RiskLevel.Info, StringBuilderEngine.buildGoal("nundo_finder").riskLevel)
        assertEquals(RiskLevel.Info, StringBuilderEngine.buildGoal("pvp_candidates").riskLevel)
        assertFalse(com.caglar.pokequery.requiresRiskWarning(StringBuilderEngine.buildGoal("hundo_check").riskLevel))
    }

    @Test
    fun `unknown goal falls back to risk level explanation`() {
        val explanation = RiskExplanations.forGoal("missing_goal", RiskLevel.Medium)
        assertEquals("risk_medium_action_adjacent", explanation.id)
    }
}
