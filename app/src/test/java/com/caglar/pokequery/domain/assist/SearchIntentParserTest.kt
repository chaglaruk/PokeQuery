package com.caglar.pokequery.domain.assist

import com.caglar.pokequery.domain.lint.ExpertCopyPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for SearchIntentParser — the natural-language search assistant.
 *
 * Covers all required patterns, edge cases, combined intents,
 * and the canBuild / explanation contract.
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
        // Check explanation/limitations contain exact or 4*
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
