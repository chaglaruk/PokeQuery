package com.caglar.pokequery.audit

import com.caglar.pokequery.domain.assist.SearchIntentParser
import com.caglar.pokequery.domain.assist.SearchStringExplainer
import com.caglar.pokequery.domain.engine.GoalStringBuilder
import com.caglar.pokequery.domain.engine.StringBuilderEngine
import com.caglar.pokequery.domain.lint.Linter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreAabAuditRegressionTest {

    @Test
    fun `negation is scoped to the matched clause`() {
        val parsed = SearchIntentParser.parse("Find hundos and exclude shinies")
        assertEquals("4*&!shiny", parsed.rawQuery)
        assertTrue("4*" in parsed.tokens)
        assertTrue("shiny" in parsed.exclusions)
        assertFalse("4*" in parsed.exclusions)
    }

    @Test
    fun `localized goal protections are not duplicated`() {
        val base = StringBuilderEngine.buildGoal("safe_cleanup", language = "Turkish")
        val merged = GoalStringBuilder.buildFinal(
            base,
            optionalProtections = listOf("shiny", "legendary", "traded"),
            language = "Turkish"
        )
        val tokens = merged.rawSyntax.split('&')
        assertEquals(1, tokens.count { it == "!parlak" })
        assertEquals(1, tokens.count { it == "!efsanevi" })
        assertEquals(1, tokens.count { it == "!takas edilen" })
    }

    @Test
    fun `count protection checks exact tokens not substrings`() {
        val otherProtections = StringBuilderEngine.COUNT_MANDATORY_PROTECTIONS
            .filterNot { it == "shiny" }
            .joinToString("&") { "!$it" }
        val warnings = Linter.lint("count2-&!shinyx&$otherProtections")
        assertTrue(warnings.any { it.isError && "!shiny" in it.message })
    }

    @Test
    fun `unsupported untraded token is blocked`() {
        val warnings = Linter.lint("!untraded")
        assertTrue(warnings.any { it.isError && "untraded" in it.message })
    }

    @Test
    fun `standard range tokens are explained as known structured filters`() {
        val explained = SearchStringExplainer.explain("count2-&0-1attack&3-4defense&3-4hp")
        assertFalse(explained.hasUnknownTokens)
        assertTrue(explained.tokens.any { it.token == "count2-" && it.category == "count_filter" })
        assertTrue(explained.tokens.any { it.token == "0-1attack" && it.category == "iv_stat" })
        assertTrue(explained.tokens.any { it.token == "3-4defense" && it.category == "iv_stat" })
        assertTrue(explained.tokens.any { it.token == "3-4hp" && it.category == "iv_stat" })
    }
}
