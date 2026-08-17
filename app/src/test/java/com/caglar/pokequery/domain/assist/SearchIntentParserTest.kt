package com.caglar.pokequery.domain.assist

import com.caglar.pokequery.domain.lint.ExpertCopyPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for SearchIntentParser - the natural-language search assistant.
 *
 * Covers all required patterns, edge cases, combined intents,
 * contrast polarity, negated controls, and the canBuild / explanation contract.
 */
class SearchIntentParserTest {

    @Test
    fun testUntagged() {
        val result = SearchIntentParser.parse("untagged")
        assertEquals("!#", result.rawQuery)
        assertTrue(result.canBuild)
        assertTrue(result.tokens.isEmpty())
        assertEquals(listOf("#"), result.exclusions)
    }

    @Test
    fun testCleanup() {
        val result = SearchIntentParser.parse("cleanup")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("1*"))
    }

    @Test
    fun testTrade() {
        val result = SearchIntentParser.parse("trade")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("count2-"))
        assertTrue(result.exclusions.contains("traded"))
    }

    @Test
    fun testPvp() {
        val result = SearchIntentParser.parse("pvp")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("0-1attack"))
        assertTrue(result.tokens.contains("3-4defense"))
        assertTrue(result.tokens.contains("3-4hp"))
    }

    @Test
    fun testGreatLeaguePvp() {
        val result = SearchIntentParser.parse("great league pvp")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("0-1attack"))
        assertTrue(result.tokens.contains("3-4defense"))
        assertTrue(result.tokens.contains("3-4hp"))
        assertTrue(result.tokens.contains("cp-1500"))
    }

    @Test
    fun testUltraLeaguePvp() {
        val result = SearchIntentParser.parse("ultra league pvp")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("0-1attack"))
        assertTrue(result.tokens.contains("3-4defense"))
        assertTrue(result.tokens.contains("3-4hp"))
        assertTrue(result.tokens.contains("cp-2500"))
    }

    @Test
    fun testLuckyTrade() {
        val result = SearchIntentParser.parse("lucky trade")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("age365-"))
        assertTrue(result.exclusions.contains("traded"))
    }

    @Test
    fun testTurkishIntentPhrases() {
        assertTrue(SearchIntentParser.parse("parlak pokemon bul").tokens.contains("shiny"))
        assertTrue(SearchIntentParser.parse("güçlü pokemon bul").tokens.contains("4*"))
        assertTrue(SearchIntentParser.parse("sansli takas").tokens.contains("age365-"))
        assertTrue(SearchIntentParser.parse("etiketlenmemis pokemon").exclusions.contains("#"))
        assertTrue(SearchIntentParser.parse("pvp adayi").tokens.contains("0-1attack"))
    }

    @Test
    fun testShinyLegendary() {
        val result = SearchIntentParser.parse("shiny legendary")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("shiny"))
        assertTrue(result.tokens.contains("legendary"))
    }

    @Test
    fun testCandyPrep() {
        val result = SearchIntentParser.parse("candy prep")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("count2-"))
    }

    @Test
    fun testNundo() {
        val result = SearchIntentParser.parse("nundo")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("0attack"))
        assertTrue(result.tokens.contains("0defense"))
        assertTrue(result.tokens.contains("0hp"))
    }

    @Test
    fun testHundo() {
        val result = SearchIntentParser.parse("hundo")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertTrue(result.explanation.contains("exact 100%") || result.explanation.contains("perfect 15/15/15"))
    }

    @Test
    fun testAllPokemon() {
        val result = SearchIntentParser.parse("all pokemon")
        assertFalse(result.canBuild)
    }

    @Test
    fun testHideShinyAndFavourites() {
        val result = SearchIntentParser.parse("hide shiny and favourites")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertTrue(result.exclusions.contains("favorite"))
        assertFalse(result.tokens.contains("shiny"))
        assertFalse(result.tokens.contains("favorite"))
        assertEquals("!shiny&!favorite", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testFindHundosAndExcludeShinies() {
        val result = SearchIntentParser.parse("Find hundos and exclude shinies")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertFalse(result.tokens.contains("shiny"))
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.exclusions.contains("4*"))
        assertEquals("4*&!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testExcludeShiniesAndFindHundos() {
        val result = SearchIntentParser.parse("exclude shinies and find hundos")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertFalse(result.tokens.contains("shiny"))
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.exclusions.contains("4*"))
        assertEquals("4*&!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    // PQ-ZAI-FINAL-01: Negated controls
    @Test
    fun testDontHideShiny() {
        val result = SearchIntentParser.parse("don't hide shiny")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("shiny"))
        assertFalse(result.exclusions.contains("shiny"))
        assertEquals("shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testDontHideShinyVariants() {
        val r1 = SearchIntentParser.parse("dont hide shiny")
        assertTrue(r1.tokens.contains("shiny"))
        assertFalse(r1.exclusions.contains("shiny"))
        assertEquals("shiny", r1.rawQuery)

        val r2 = SearchIntentParser.parse("do not hide shiny")
        assertTrue(r2.tokens.contains("shiny"))
        assertFalse(r2.exclusions.contains("shiny"))
        assertEquals("shiny", r2.rawQuery)
    }

    @Test
    fun testDontIncludeShiny() {
        val result = SearchIntentParser.parse("don't include shiny")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.tokens.contains("shiny"))
        assertEquals("!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testNotShiny() {
        val result = SearchIntentParser.parse("not shiny")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.tokens.contains("shiny"))
        assertEquals("!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    // PQ-ZAI-FINAL-02: Contrast with explicit positive control
    @Test
    fun testWithoutShinyButWithHundo() {
        val result = SearchIntentParser.parse("without shiny but with hundo")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertFalse(result.tokens.contains("shiny"))
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.exclusions.contains("4*"))
        assertEquals("4*&!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    // PQ-ZAI-FINAL-03: Contrast polarity inheritance
    @Test
    fun testShowAllButHundos() {
        val result = SearchIntentParser.parse("show all but hundos")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("4*"))
        assertFalse(result.tokens.contains("4*"))
        assertEquals("!4*", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testFindAllButShiny() {
        val result = SearchIntentParser.parse("find all but shiny")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.tokens.contains("shiny"))
        assertEquals("!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testEverythingButShinyAndLegendary() {
        val result = SearchIntentParser.parse("everything but shiny and legendary")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertTrue(result.exclusions.contains("legendary"))
        assertTrue(result.tokens.isEmpty())
        assertEquals("!shiny&!legendary", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testShowAllButShinyAndHundos() {
        val result = SearchIntentParser.parse("show all but shiny and hundos")
        assertTrue(result.canBuild)
        assertTrue(result.exclusions.contains("shiny"))
        assertTrue(result.exclusions.contains("4*"))
        assertTrue(result.tokens.isEmpty())
        assertTrue(result.rawQuery == "!4*&!shiny" || result.rawQuery == "!shiny&!4*")
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testHideShinyButHundo() {
        val result = SearchIntentParser.parse("hide shiny but hundo")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("4*"))
        assertFalse(result.tokens.contains("shiny"))
        assertTrue(result.exclusions.contains("shiny"))
        assertFalse(result.exclusions.contains("4*"))
        assertEquals("4*&!shiny", result.rawQuery)
        assertFalse(result.rawQuery.contains("|"))
    }

    @Test
    fun testShadowPokemonForTrade() {
        val result = SearchIntentParser.parse("shadow pokemon for trade")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("shadow"))
        assertTrue(result.limitations.any { it.contains("cannot be traded", ignoreCase = true) })
    }

    @Test
    fun testPurifiedGuaranteedLuckyGreatLeague() {
        val result = SearchIntentParser.parse("purified pokemon that may be good for guaranteed lucky trade and have Great League IVs")
        assertTrue(result.canBuild)
        assertTrue(result.tokens.contains("purified"))
        assertTrue(result.tokens.contains("age365-"))
        assertTrue(result.tokens.contains("0-1attack"))
        assertTrue(result.tokens.contains("cp-1500"))
        assertTrue(result.exclusions.contains("traded"))
        assertTrue(result.limitations.isNotEmpty())
    }

    @Test
    fun testNoRawQueryContainsInvalidFormatting() {
        val inputs = listOf(
            "untagged", "cleanup", "trade", "pvp", "shiny legendary", "hundo",
            "nundo", "candy prep", "lucky trade", "old", "distance", "shadow",
            "purified", "lucky", "costume", "favorite", "mythical", "legendary",
            "great league pvp", "ultra league pvp"
        )
        for (input in inputs) {
            val result = SearchIntentParser.parse(input)
            if (result.canBuild) {
                assertFalse("'$input' rawQuery should not contain '!&!#': ${result.rawQuery}",
                    result.rawQuery.contains("!&!#"))
                assertFalse("'$input' rawQuery should not contain '&&': ${result.rawQuery}",
                    result.rawQuery.contains("&&"))
                assertFalse("'$input' rawQuery should not start with '&': ${result.rawQuery}",
                    result.rawQuery.startsWith("&"))
                assertFalse("'$input' rawQuery should not end with '&': ${result.rawQuery}",
                    result.rawQuery.endsWith("&"))
            }
        }
    }
}
