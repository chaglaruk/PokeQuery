package com.caglar.pokequery.domain.engine

import com.caglar.pokequery.data.model.RiskLevel
import com.caglar.pokequery.domain.lint.Linter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StringBuilderEngineTest {

    @Test
    fun `no generated default string contains pipe`() {
        val result = StringBuilderEngine.buildString(
            baseQuery = "test|query", 
            explanation = "test"
        )
        assertFalse(result.rawSyntax.contains("|"))
        assertTrue(result.rawSyntax.contains(","))
    }

    @Test
    fun `count templates include required exclusions`() {
        val result = StringBuilderEngine.buildString(
            baseQuery = "count2-", 
            protections = emptyList(), // no optional protections
            explanation = "test"
        )
        // mandatory protections for count should still apply
        assertTrue(result.rawSyntax.contains("!shiny"))
        assertTrue(result.rawSyntax.contains("!legendary"))
        assertTrue(result.rawSyntax.contains("!costume"))
    }

    @Test
    fun `safe cleanup includes default exclusions`() {
        val result = StringBuilderEngine.buildString(
            baseQuery = "1*", 
            explanation = "test"
        )
        assertTrue(result.rawSyntax.contains("!shiny"))
        assertTrue(result.rawSyntax.contains("!4*"))
        assertFalse(result.rawSyntax.contains("!0*")) // 0* is not in DEFAULT_PROTECTIONS anymore based on new spec
    }

    @Test
    fun `2x candy prep includes count2- and warning`() {
        val result = StringBuilderEngine.buildString(
            baseQuery = "count2-", 
            explanation = "test"
        )
        assertTrue(result.rawSyntax.contains("count2-"))
        assertEquals(RiskLevel.Medium, result.riskLevel)
    }

    @Test
    fun `linter catches pipe`() {
        val warnings = Linter.lint("shiny|lucky")
        assertTrue(warnings.any { it.message.contains("|") })
    }

    @Test
    fun `linter catches unsafe count`() {
        val warnings = Linter.lint("count3-")
        assertTrue(warnings.any { it.message.contains("Unsafe count usage") })
        assertTrue(warnings.any { it.message.contains("!costume") })
    }

    @Test
    fun `linter catches count shortcut risky inclusion and reserved tag collision`() {
        val warnings = Linter.lint("count&shiny&#shiny")
        assertTrue(warnings.any { it.message.contains("count2-") })
        assertTrue(warnings.any { it.message.contains("Risky inclusion of shiny") })
        assertTrue(warnings.any { it.message.contains("collides") })
    }

    @Test
    fun `engine adds count warning`() {
        val result = StringBuilderEngine.buildString(baseQuery = "count2-", explanation = "test")
        assertTrue(result.warnings.any { it.contains("Count is based on Pokédex species number") })
    }

    @Test
    fun `trade fodder includes trade warning and correct protections`() {
        val result = StringBuilderEngine.buildGoal("trade_fodder")
        assertTrue(result.warnings.any { it.contains("Real trade eligibility depends on friendship level") })
        assertTrue(result.warnings.any { it.contains("Count is based on Pokédex species number") })
        assertFalse(result.rawSyntax == "traded")
        assertTrue(result.rawSyntax.contains("!traded"))
        
        // Ensure rawString is indeed the full string with all protections
        assertTrue(result.rawSyntax.contains("!shiny"))
        assertTrue(result.rawSyntax.contains("!lucky"))
        assertTrue(result.rawSyntax.contains("!legendary"))
    }

    @Test
    fun `safe cleanup includes positive condition and review explanation`() {
        val explanation = "This is a REVIEW string targeting 1-star low-value candidates. It is not an automatic transfer command."
        val result = StringBuilderEngine.buildString(baseQuery = "1*", explanation = explanation)
        assertTrue(result.rawSyntax.contains("1*"))
        assertTrue(result.rawSyntax.contains("!shiny"))
        assertTrue(result.rawSyntax.contains("!4*"))
        assertTrue(result.plainLanguageExplanation.contains("REVIEW string"))
        assertTrue(result.plainLanguageExplanation.contains("not an automatic transfer"))
    }

    @Test
    fun `hundo check does not hide special categories and has correct explanation`() {
        // Simulating Navigation.kt behavior where protections are cleared
        val explanation = "Finds all perfect IV / hundo Pokémon. 4★ means 15/15/15."
        val result = StringBuilderEngine.buildGoal("hundo_check")
        
        assertEquals("4*", result.rawSyntax)
        assertFalse(result.rawSyntax.contains("!shiny"))
        assertFalse(result.rawSyntax.contains("!legendary"))
        assertFalse(result.rawSyntax.contains("!shadow"))
        assertFalse(result.rawSyntax.contains("!lucky"))
        assertFalse(result.rawSyntax.contains("!costume"))
        assertFalse(result.rawSyntax.contains("!#"))
        assertFalse(result.rawSyntax.contains("!traded"))
        
        assertTrue(result.plainLanguageExplanation.contains("perfect IV / hundo"))
        assertTrue(result.plainLanguageExplanation.contains("15/15/15"))
        assertEquals(RiskLevel.Info, result.riskLevel)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun `all default count goals exclude costume`() {
        listOf("candy_prep", "trade_fodder").forEach { goal ->
            assertTrue("$goal must exclude costume", StringBuilderEngine.buildGoal(goal).rawSyntax.contains("!costume"))
        }
    }

    @Test
    fun `nundo finder generates exact string without protections`() {
        val result = StringBuilderEngine.buildGoal("nundo_finder")
        assertEquals("0attack&0defense&0hp", result.rawSyntax)
        assertFalse(result.rawSyntax.contains("!shiny"))
        assertEquals(RiskLevel.Info, result.riskLevel)
        assertEquals("Very Narrow", result.scopeBreadth)
    }

    @Test
    fun `pvp candidates generate correctly without cleanup protections`() {
        val greatLeague = StringBuilderEngine.buildGoal("pvp_candidates", config = "great")
        val ultraLeague = StringBuilderEngine.buildGoal("pvp_candidates", config = "ultra")
        
        assertEquals("0-1attack&3-4defense&3-4hp&cp-1500", greatLeague.rawSyntax)
        assertEquals("0-1attack&3-4defense&3-4hp&cp-2500", ultraLeague.rawSyntax)
        assertFalse(greatLeague.rawSyntax.contains("!shiny"))
        assertEquals("Narrow", greatLeague.scopeBreadth)
    }

    @Test
    fun `lucky trade prep generates age or distance modes with warnings`() {
        val ageMode = StringBuilderEngine.buildGoal("lucky_trade", config = "age")
        val distMode = StringBuilderEngine.buildGoal("lucky_trade", config = "distance")
        
        assertEquals("age365-&!traded", ageMode.rawSyntax)
        assertEquals("distance100-&!traded", distMode.rawSyntax)
        assertFalse(ageMode.rawSyntax.contains("!shiny")) // valuable pokemon can be traded
        assertEquals("Moderate", ageMode.scopeBreadth)
    }

    @Test
    fun `linter ignores exact nundo pattern for 0 star warning`() {
        val warnings = Linter.lint("0attack&0defense&0hp")
        assertFalse(warnings.any { it.message.contains("0* is an IV band") })
        
        val warningsWith0Star = Linter.lint("0*")
        assertTrue(warningsWith0Star.any { it.message.contains("0* is an IV band") })
    }

    @Test
    fun `linter bypasses transfer warnings for pvp and trade prep`() {
        val pvpWarnings = Linter.lint("0-1attack&3-4defense&3-4hp&cp-1500&shiny") // User typed shiny in pvp search
        assertFalse(pvpWarnings.any { it.message.contains("Risky inclusion") })

        val tradePrepWarnings = Linter.lint("age365-&!traded")
        assertTrue(tradePrepWarnings.any { it.message.contains("Trade prep search") })
    }
}
