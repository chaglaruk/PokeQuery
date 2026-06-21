package com.caglar.pokequery.domain.risk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Package 4 — per-goal risk explanation builder.
 *
 * RiskWarning copy must change depending on goalId (and surface a Turkish caution
 * when the output is Turkish). Pure unit tests pin the wording per goal.
 */
class RiskMessageBuilderTest {

    @Test
    fun `safe cleanup explains exclusions plus manual review`() {
        val msg = RiskMessageBuilder.messageFor("safe_cleanup", turkish = false)
        assertTrue(msg.contains("excludes", ignoreCase = true) || msg.contains("protected", ignoreCase = true))
        assertTrue(msg.contains("review", ignoreCase = true))
    }

    @Test
    fun `candy prep warns about event use`() {
        val msg = RiskMessageBuilder.messageFor("candy_prep", turkish = false)
        assertTrue(msg.contains("candy", ignoreCase = true))
        assertTrue(msg.contains("event", ignoreCase = true))
    }

    @Test
    fun `trade fodder explains trade eligibility cannot be guaranteed`() {
        val msg = RiskMessageBuilder.messageFor("trade_fodder", turkish = false)
        assertTrue(msg.contains("trade", ignoreCase = true))
        assertTrue(msg.contains("cannot", ignoreCase = true) || msg.contains("not guaranteed", ignoreCase = true))
    }

    @Test
    fun `lucky trade warns valuables may appear`() {
        val msg = RiskMessageBuilder.messageFor("lucky_trade", turkish = false)
        assertTrue(msg.contains("lucky", ignoreCase = true))
        assertTrue(msg.contains("valuable", ignoreCase = true) || msg.contains("review", ignoreCase = true))
    }

    @Test
    fun `pvp candidates explains candidate-only and dedicated tools`() {
        val msg = RiskMessageBuilder.messageFor("pvp_candidates", turkish = false)
        assertTrue(msg.contains("candidate", ignoreCase = true))
        assertTrue(msg.contains("tools", ignoreCase = true) || msg.contains("verify", ignoreCase = true))
    }

    @Test
    fun `hundo and nundo explained as inspection not cleanup`() {
        val hundo = RiskMessageBuilder.messageFor("hundo_check", turkish = false)
        val nundo = RiskMessageBuilder.messageFor("nundo_finder", turkish = false)
        assertTrue(hundo.contains("inspection", ignoreCase = true))
        assertTrue(nundo.contains("inspection", ignoreCase = true))
        assertFalse(hundo.contains("transfer", ignoreCase = true))
    }

    @Test
    fun `turkish caution appears when output is turkish`() {
        val msg = RiskMessageBuilder.messageFor("safe_cleanup", turkish = true)
        assertTrue(msg.contains("Turkish", ignoreCase = true))
        assertTrue(msg.contains("beta", ignoreCase = true))
        assertTrue(msg.contains("verify", ignoreCase = true))
    }

    @Test
    fun `turkish caution does not appear for english output`() {
        val msg = RiskMessageBuilder.messageFor("safe_cleanup", turkish = false)
        assertFalse(msg.contains("Turkish search terms are beta", ignoreCase = true))
    }

    @Test
    fun `unknown goal falls back to a safe generic review message`() {
        val msg = RiskMessageBuilder.messageFor("totally_unknown_goal", turkish = false)
        assertTrue(msg.contains("review", ignoreCase = true))
    }
}
